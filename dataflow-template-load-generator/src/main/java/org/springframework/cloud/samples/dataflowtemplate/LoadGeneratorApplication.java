/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.samples.dataflowtemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.TaskOperations;
import org.springframework.cloud.dataflow.rest.client.config.DataFlowClientAutoConfiguration;
import org.springframework.cloud.dataflow.rest.resource.AppRegistrationResource;
import org.springframework.cloud.dataflow.rest.resource.CurrentTaskExecutionsResource;
import org.springframework.cloud.dataflow.rest.resource.TaskDefinitionResource;
import org.springframework.cloud.dataflow.rest.util.HttpClientConfigurer;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootApplication(exclude = { DataFlowClientAutoConfiguration.class })
public class LoadGeneratorApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(LoadGeneratorApplication.class, args);
	}

	private final Logger logger = LoggerFactory.getLogger(LoadGeneratorApplication.class);

	@Override
	public void run(ApplicationArguments applicationArguments) throws Exception {
		final DataFlowTemplate dataFlowTemplate = getDataFlowTemplate(null, null, "http://localhost:9393");

		importApps(dataFlowTemplate);

		final Map<String, String> templateTaskDefinitions = new HashMap<>();
		templateTaskDefinitions.put("timestamp", "timestamp");
		templateTaskDefinitions.put("timestamp-batch", "timestamp-batch");

		final TaskOperations taskOperations = dataFlowTemplate.taskOperations();

		final int numberOfDesiredTaskDefinitions = 100;

		final Set<TaskDefinitionResource> taskDefinitions = new HashSet<>(numberOfDesiredTaskDefinitions*templateTaskDefinitions.size());

		for (java.util.Map.Entry<String, String> entry : templateTaskDefinitions.entrySet()) {
			for (int i = 1; i <= numberOfDesiredTaskDefinitions; i++) {
				final TaskDefinitionResource createdTaskDefinition = taskOperations.create(entry.getKey() + "_" + i, entry.getValue());
				taskDefinitions.add(createdTaskDefinition);
			}
		}

		logger.info("\n\nCreated a total of {} Task Definitions.\n", taskDefinitions.size());

		final Set<Long> executions = new HashSet<>(0);
		int numberOfDesiredExecutions = 20;

		for (TaskDefinitionResource taskDefinition : taskDefinitions) {
			final Set<Long> executionsPerTaskDefinition = new HashSet<>(0);
			while (executionsPerTaskDefinition.size() < numberOfDesiredExecutions) {
				CurrentTaskExecutionsResource c = taskOperations.currentTaskExecutions().iterator().next();
				long runningExecutions = c.getRunningExecutionCount();
				long maxExecutions = c.getMaximumTaskExecutions();
				long delta = maxExecutions - runningExecutions;

				if (delta > 0) {
					long executionId = taskOperations.launch(taskDefinition.getName(), new HashMap<>(), new ArrayList<>());
					executionsPerTaskDefinition.add(executionId);
				}

				Thread.sleep(200);
			}
			executions.addAll(executionsPerTaskDefinition);
		}

		logger.info("\n\nCreated a total of {} task executions.", executions.size());
	}

	private void importApps(DataFlowTemplate dataFlowTemplate) {
		final String taskAppRegistrationUrl = "https://dataflow.spring.io/task-maven-latest";
		final String appRegistrationUrl = "https://dataflow.spring.io/rabbitmq-maven-latest";
		logger.info("\n\nImporting Stream Applications from: {}.\n", appRegistrationUrl);
		logger.info("\n\nImporting Task Applications from: {}.\n", taskAppRegistrationUrl);

		final PagedResources<AppRegistrationResource> appRegistrations =
			dataFlowTemplate.appRegistryOperations().importFromResource(appRegistrationUrl, false);

		final PagedResources<AppRegistrationResource> taskAppRegistrations =
				dataFlowTemplate.appRegistryOperations().importFromResource(taskAppRegistrationUrl, false);

		logger.info("Imported {} Stream Applications. Showing the first {} from page {}",
			appRegistrations.getMetadata().getTotalElements(),
			appRegistrations.getMetadata().getSize(),
			appRegistrations.getMetadata().getNumber());

		for (AppRegistrationResource app : appRegistrations.getContent()) {
			logger.info("{} (type: {})", app.getName(), app.getType());
		}

		logger.info("Imported {} Task Applications. Showing the first {} from page {}",
				taskAppRegistrations.getMetadata().getTotalElements(),
				taskAppRegistrations.getMetadata().getSize(),
				taskAppRegistrations.getMetadata().getNumber());

		for (AppRegistrationResource app : taskAppRegistrations.getContent()) {
			logger.info("{} (type: {})", app.getName(), app.getType());
		}

	}

	private static DataFlowTemplate getDataFlowTemplate(String username, String password, String target) {
		final URI targetUri = URI.create(target);

		final RestTemplate restTemplate = DataFlowTemplate.getDefaultDataflowRestTemplate();

		if (username != null && password != null) {
			final HttpClientConfigurer httpClientConfigurer = HttpClientConfigurer.create(targetUri);
			httpClientConfigurer.basicAuthCredentials(username, password);
			restTemplate.setRequestFactory(httpClientConfigurer.buildClientHttpRequestFactory());
		}

		final DataFlowTemplate dataFlowTemplate = new DataFlowTemplate(targetUri, restTemplate);

		return dataFlowTemplate;
	}
}
