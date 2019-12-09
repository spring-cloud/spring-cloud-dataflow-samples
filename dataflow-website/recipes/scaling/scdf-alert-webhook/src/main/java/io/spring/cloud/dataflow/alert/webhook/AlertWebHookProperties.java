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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Christian Tzolov
 */
@ConfigurationProperties("scdf.alert.webhook")
public class AlertWebHookProperties {

	/**
	 * Number of application instances to scale out to in case of scale out alert event.
	 */
	private int scaleOutFactor = 3;

	/**
	 * Number of application instances to scale in to in case of scale in alert event.
	 */
	private int scaleInFactor = 1;

	/**
	 * Name of the stream containing the application to be scaled.
	 * If scaleStreamName is empty then an attempt will be made to resolve the stream name from alert's label
	 * called 'stream_name'.
	 */
	private String scaleStreamName;

	/**
	 * Stream application name (as appears in the stream definition) to scale out or in.
	 * If scaleApplicationName is empty then an attempt will be made to resolve the stream name from alert's label
	 * called 'application_name'.
	 */
	private String scaleApplicationName;

	/**
	 * Name of the Alert used to trigger that application scale out.
	 * The alert is defined in the Prometheus alert.rules.yml configuration.
	 */
	private String scaleOutAlertName;

	/**
	 * Name of the Alert used to trigger that application scale in.
	 * The alert is defined in the Prometheus alert.rules.yml configuration.
	 */
	private String scaleInAlertName;

	public int getScaleOutFactor() {
		return scaleOutFactor;
	}

	public void setScaleOutFactor(int scaleOutFactor) {
		this.scaleOutFactor = scaleOutFactor;
	}

	public String getScaleOutAlertName() {
		return scaleOutAlertName;
	}

	public void setScaleOutAlertName(String scaleOutAlertName) {
		this.scaleOutAlertName = scaleOutAlertName;
	}

	public String getScaleInAlertName() {
		return scaleInAlertName;
	}

	public void setScaleInAlertName(String scaleInAlertName) {
		this.scaleInAlertName = scaleInAlertName;
	}

	public int getScaleInFactor() {
		return scaleInFactor;
	}

	public void setScaleInFactor(int scaleInFactor) {
		this.scaleInFactor = scaleInFactor;
	}

	public String getScaleStreamName() {
		return scaleStreamName;
	}

	public void setScaleStreamName(String scaleStreamName) {
		this.scaleStreamName = scaleStreamName;
	}

	public String getScaleApplicationName() {
		return scaleApplicationName;
	}

	public void setScaleApplicationName(String scaleApplicationName) {
		this.scaleApplicationName = scaleApplicationName;
	}
}
