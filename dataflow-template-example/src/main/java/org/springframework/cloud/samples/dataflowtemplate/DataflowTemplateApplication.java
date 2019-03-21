/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.samples.dataflowtemplate;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.config.DataFlowClientAutoConfiguration;
import org.springframework.cloud.dataflow.rest.resource.AppRegistrationResource;
import org.springframework.cloud.dataflow.rest.util.HttpClientConfigurer;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootApplication(exclude = { DataFlowClientAutoConfiguration.class })
public class DataflowTemplateApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(DataflowTemplateApplication.class, args);
	}

	private final Logger logger = LoggerFactory.getLogger(DataflowTemplateApplication.class);

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		final DataFlowTemplate dataFlowTemplate = getDataFlowTemplate("user", "secret99", "http://localhost:9393");

		final String appRegistrationUrl = "https://bit.ly/Einstein-RC1-stream-applications-rabbit-maven";
		logger.info("\n\nImporting Applications from: {}.\n", appRegistrationUrl);

		final PagedResources<AppRegistrationResource> appRegistrations =
			dataFlowTemplate.appRegistryOperations().importFromResource(appRegistrationUrl, false);

		logger.info("Imported {} Applications. Showing the first {} from page {}",
			appRegistrations.getMetadata().getTotalElements(),
			appRegistrations.getMetadata().getSize(),
			appRegistrations.getMetadata().getNumber());

		for (AppRegistrationResource app : appRegistrations.getContent()) {
			logger.info("{} (type: {})", app.getName(), app.getType());
		}


	}

	private static DataFlowTemplate getDataFlowTemplate(String username, String password, String target) {
		final URI targetUri = URI.create(target);

		final RestTemplate restTemplate = DataFlowTemplate.getDefaultDataflowRestTemplate();
		final HttpClientConfigurer httpClientConfigurer = HttpClientConfigurer.create(targetUri);
		httpClientConfigurer.basicAuthCredentials(username, password);
		restTemplate.setRequestFactory(httpClientConfigurer.buildClientHttpRequestFactory());

		final DataFlowTemplate dataFlowTemplate = new DataFlowTemplate(targetUri, restTemplate);

		return dataFlowTemplate;
	}
}
