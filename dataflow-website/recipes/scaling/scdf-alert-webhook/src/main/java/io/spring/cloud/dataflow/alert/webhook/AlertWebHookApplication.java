/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.cloud.dataflow.alert.webhook;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.dataflow.rest.client.DataFlowOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Elastic, Stream auto-scaling adapter for Spring Cloud DataFlow.
 *
 * The {@link AlertWebHookApplication} is configured as a Prometheus Alertmanager Webhook Receiver (https://github.com/prometheus/alertmanager),
 * that listens for pre-configured Prometheus alerts and leverages the SCDF Scale API to scale out or in a preconfigured
 * stream application.
 *
 * The {@link AlertWebHookApplication} listens for scale-out and scale-in alerts, configured by the
 * {@link AlertWebHookProperties#getScaleOutAlertName} and the {@link AlertWebHookProperties#getScaleInAlertName}.
 *
 * On scale-out alert it increases the number of application instances to {@link AlertWebHookProperties#getScaleOutFactor} instances.
 *
 * On scale-in alert it decreases the number of application instances to {@link AlertWebHookProperties#getScaleInFactor} instances.
 *
 * The stream name should be provided either as a {@link AlertWebHookProperties#getScaleStreamName} property or an
 * Alert label called: 'stream_name'
 *
 * The application name should be provided either as a {@link AlertWebHookProperties#getScaleApplicationName} property or an
 * Alert label called: 'application_name'
 *
 * Internally the {@link AlertWebHookApplication} uses the Data Flow Scale REST API to scale the app instances in platform agnostic way.
 * Use the spring.cloud.dataflow.client.server-uri property to configure url of the Data Flow server.
 *
 * @author Christian Tzolov
 */

@SpringBootApplication
@RestController
@EnableConfigurationProperties(AlertWebHookProperties.class)
public class AlertWebHookApplication {

	private static final Logger logger = LoggerFactory.getLogger(AlertWebHookApplication.class);

	public static final String FIRING_ALERT = "firing";
	public static final String ALERT_NAME = "alertname";
	public static final String ALERT_LABELS = "labels";
	public static final String ALERT_STATUS = "status";
	public static final String ALERTS = "alerts";
	public static final String STREAM_NAME = "stream_name";
	public static final String APPLICATION_NAME = "application_name";

	private AlertWebHookProperties properties;

	// Use the spring.cloud.dataflow.client.server-uri property to configure url of the Data Flow server.
	private final DataFlowOperations dataFlowOperations;

	@Autowired
	public AlertWebHookApplication(DataFlowOperations dataFlowOperations, AlertWebHookProperties properties) {
		this.properties = properties;
		this.dataFlowOperations = dataFlowOperations;
	}

	/**
	 * This method is called by the AlertManager passing a JSON payload as documented here:
	 * https://prometheus.io/docs/alerting/configuration/#webhook_config
	 */
	@RequestMapping(value = "/alert", method = RequestMethod.POST)
	public void alertMessage(@RequestBody Map<String, Object> payload) {

		// Extract all alerts received with this message.
		List<Map<String, Object>> alerts = (List<Map<String, Object>>) payload.get(ALERTS);

		if (CollectionUtils.isEmpty(alerts)) {
			return; // nothing to do
		}

		for (Map<String, Object> alert : alerts) {
			String alertStatus = (String) alert.get(ALERT_STATUS);

			// Act only upon "firing" alert types
			if (alertStatus.equalsIgnoreCase(FIRING_ALERT)) {

				Map<String, String> labels = (Map<String, String>) alert.get(ALERT_LABELS);
				String alertName = labels.get(ALERT_NAME); // automatically set by the alert manager.

				String applicationName = StringUtils.isEmpty(this.properties.getScaleApplicationName()) ?
						labels.get(APPLICATION_NAME) : this.properties.getScaleApplicationName();
				String streamName = StringUtils.isEmpty(this.properties.getScaleStreamName()) ?
						labels.get(STREAM_NAME) : this.properties.getScaleStreamName();

				if (alertName.equalsIgnoreCase(this.properties.getScaleOutAlertName())) {
					logger.info(String.format("Scale Out: %s, %s to %s", streamName, applicationName,
							this.properties.getScaleOutFactor()));
					this.scale(streamName, applicationName, this.properties.getScaleOutFactor());
				}
				else if (alertName.equalsIgnoreCase(this.properties.getScaleInAlertName())) {
					logger.info(String.format("Scale In: %s, %s to %s", streamName, applicationName,
							this.properties.getScaleOutFactor()));
					this.scale(streamName, applicationName, this.properties.getScaleInFactor());
				}
			}
		}
	}

	// Spring Cloud Data Flow Scale REST API
	private void scale(String streamName, String appName, int scale) {
		this.dataFlowOperations.streamOperations().scaleApplicationInstances(streamName, appName, scale,
				Collections.emptyMap());
	}

	public static void main(String[] args) {
		SpringApplication.run(AlertWebHookApplication.class, args);
	}
}
