/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.dataflow.prometheus.servicediscovery;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.rest.resource.AppStatusResource;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

/**
 * Prometheus Service Discovery for SCSt apps deployed with the Local Deployer (?k8s?). It is build on top of the
 * file_sd_config mechanism
 *
 * https://prometheus.io/docs/prometheus/latest/configuration/configuration/#%3Cfile_sd_config%3E
 *
 * @author Christian Tzolov
 */
@SpringBootApplication
@EnableScheduling
public class DataflowPrometheusServiceDiscoveryApplication {

	private final Logger logger = LoggerFactory.getLogger(DataflowPrometheusServiceDiscoveryApplication.class);
	private final ObjectMapper objectMapper;
	private final RestTemplate restTemplate;


	public enum TargetMode {local, promregator}

	/**
	 * Url of the the SCDF Runtime REST endpoint:
	 * https://docs.spring.io/spring-cloud-dataflow/docs/current/reference/htmlsingle/#api-guide-resources-runtime-information-applications
	 */
	@Value("${metrics.prometheus.target.discovery.url:http://localhost:9393/runtime/apps}")
	private String targetDiscoveryUrl;

	/**
	 * Path where the generated targets.json file is stored.
	 */
	@Value("${metrics.prometheus.target.file.path:/tmp/targets.json}")
	private String outputTargetFilePath;

	// For k8s deployment use 'pod.ip' instead of 'url'
	@Value("${metrics.prometheus.target.dataflow.runtime.attribute.name:url}")
	private String attributeName;

	@Value("${metrics.prometheus.target.mode:local}")
	private TargetMode targetMode;

	public DataflowPrometheusServiceDiscoveryApplication() {
		this.objectMapper = Jackson2ObjectMapperBuilder.json().modules(new Jackson2HalModule()).build();
		this.restTemplate = new RestTemplate(Arrays.asList(new MappingJackson2HttpMessageConverter(this.objectMapper)));

	}

	public static void main(String[] args) {
		SpringApplication.run(DataflowPrometheusServiceDiscoveryApplication.class, args);
	}

	/**
	 * Use the metrics.prometheus.target.refresh.rate property (in milliseconds) to change the targets discovery rate.
	 *
	 * @throws IOException
	 */
	@Scheduled(fixedRateString = "${metrics.prometheus.target.refresh.rate:15000}")
	public void updateTargets() throws IOException {

		String targetsJson;
		if (this.targetMode == TargetMode.local) {
			targetsJson = this.findTargetsFromDataflowRuntimeApps();
		}
		else if (this.targetMode == TargetMode.promregator) {
			targetsJson = this.findTargetsWithPromregator();
		}
		else {
			throw new IllegalStateException("Unknown target mode:" + this.targetMode);
		}

		logger.info(this.targetMode + ": " + targetsJson);

		this.updateTargetsFile(targetsJson);
	}

	public String findTargetsFromDataflowRuntimeApps() throws IOException {

		AppStatusResource.Page page = this.restTemplate.getForObject(this.targetDiscoveryUrl, AppStatusResource.Page.class);

		List<String> targetUrls = page.getContent().stream()
				.map(appResource -> appResource.getInstances().getContent())
				.flatMap(instances -> instances.stream().map(inst -> inst.getAttributes().get(this.attributeName)))
				.map(url -> url.replace("http://", ""))
				.collect(Collectors.toList());

		return this.buildPrometheusTargetsJson(targetUrls);
	}

	private String findTargetsWithPromregator() throws IOException {
		Object b = new RestTemplate().getForObject(this.targetDiscoveryUrl, Object.class);
		return this.objectMapper.writeValueAsString(b);
	}

	/**
	 * Converts a list of urls into JSON list of static target configs, compliant with the file_sd_config format.
	 * @param targetUrls list of SCSt apps ip:ports to be used as prometheus metrics targets.
	 * @return json record compliant with file_sd_config
	 * @throws JsonProcessingException
	 */
	private String buildPrometheusTargetsJson(List<String> targetUrls) throws JsonProcessingException {
		StaticConfig staticConfig = new StaticConfig();
		staticConfig.setTargets(targetUrls);
		staticConfig.setLabels(Collections.singletonMap("job", "scdf"));
		return this.objectMapper.writeValueAsString(Arrays.asList(staticConfig));
	}

	/**
	 * Re-writes the updated targets json into output file used by Prometheus.
	 * @param targetsJson SCDF apps targets
	 * @throws IOException
	 */
	private void updateTargetsFile(String targetsJson) throws IOException {
		FileWriter fw = new FileWriter(this.outputTargetFilePath);
		fw.write(targetsJson);
		fw.close();
	}

	/**
	 * Java model used as JSON representation of Prometheus' static target format.
	 * https://prometheus.io/docs/prometheus/latest/configuration/configuration/#%3Cfile_sd_config%3E
	 */
	public static class StaticConfig {
		private List<String> targets;
		private Map<String, String> labels;

		public List<String> getTargets() {
			return targets;
		}

		public void setTargets(List<String> targets) {
			this.targets = targets;
		}

		public Map<String, String> getLabels() {
			return labels;
		}

		public void setLabels(Map<String, String> labels) {
			this.labels = labels;
		}
	}
}
