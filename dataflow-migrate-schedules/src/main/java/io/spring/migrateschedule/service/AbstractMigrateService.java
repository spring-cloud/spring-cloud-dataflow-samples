/*
 * Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.migrateschedule.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.cloud.dataflow.core.TaskDefinition;
import org.springframework.cloud.dataflow.registry.support.AppResourceCommon;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Abstract class containing methods that will be required for both Cloud Foundry
 * and the Kubernetes Impls.
 *
 * @author Glenn Renfro
 */
public abstract class AbstractMigrateService implements MigrateScheduleService {

	private final static String DATA_FLOW_URI_KEY = "spring.cloud.dataflow.client.serverUri";

	private final static String COMMAND_ARGUMENT_PREFIX = "cmdarg.";

	protected final static String APP_PREFIX = "app.";

	protected final static String DEPLOYER_PREFIX = "deployer.";

	protected MigrateProperties migrateProperties;

	private TaskDefinitionRepository taskDefinitionRepository;

	private MavenProperties mavenProperties;

	public AbstractMigrateService(MigrateProperties migrateProperties, TaskDefinitionRepository taskDefinitionRepository, MavenProperties mavenProperties) {
		this.migrateProperties = migrateProperties;
		this.taskDefinitionRepository = taskDefinitionRepository;
		this.mavenProperties = mavenProperties;
	}

	public TaskDefinition findTaskDefinitionByName(String taskDefinitionName) {
		return this.taskDefinitionRepository.findByTaskName(taskDefinitionName);
	}

	protected String getSchedulePrefixDefinitionName(String taskDefinitionName) {
		return this.migrateProperties.getSchedulerToken() + taskDefinitionName;
	}

	/**
	 * Retain only properties that are meant for the <em>scheduler</em> of a given task(those
	 * that start with {@code scheduler.}and qualify all
	 * property values with the {@code spring.cloud.scheduler.} prefix.
	 *
	 * @param input the scheduler properties
	 * @return scheduler properties for the task
	 */
	protected static Map<String, String> extractAndQualifySchedulerProperties(Map<String, String> input) {
		final String prefix = "spring.cloud.scheduler.";

		Map<String, String> result = new TreeMap<>(input).entrySet().stream()
				.filter(kv -> kv.getKey().startsWith(prefix))
				.collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue(),
						(fromWildcard, fromApp) -> fromApp));

		return result;
	}

	/**
	 * Retrieve the resource for the SchedulerTaskLauncher and verify the URI.
	 * @return {@link Resource} for the SchedulerTaskLauncher.
	 */
	protected Resource getTaskLauncherResource() {
		final URI url;
		try {
			new URI(this.migrateProperties.getSchedulerTaskLauncherUrl()); //verify url
		}
		catch (URISyntaxException uriSyntaxException) {
			throw new IllegalArgumentException(uriSyntaxException);
		}
		AppResourceCommon appResourceCommon = new AppResourceCommon(this.mavenProperties, new DefaultResourceLoader());
		return appResourceCommon.getResource(this.migrateProperties.getSchedulerTaskLauncherUrl());
	}

	/**
	 * Add the appropriate tags to the command line args so that the SchedulerTaskLauncher can
	 * extract them.
	 * @param args the command line args to be tagged.
	 * @return the tagged command line args.
	 */
	protected List<String> tagCommandLineArgs(List<String> args) {
		List<String> taggedArgs = new ArrayList<>();

		for(String arg : args) {
			if(arg.contains("spring.cloud.task.name")) {
				continue;
			}
			String updatedArg = arg;
			if (!arg.startsWith(DATA_FLOW_URI_KEY) && !"--".concat(arg).startsWith(DATA_FLOW_URI_KEY)) {
				updatedArg = COMMAND_ARGUMENT_PREFIX +
						this.migrateProperties.getTaskLauncherPrefix() + "." + arg;
			}
			taggedArgs.add(updatedArg);
		}
		return taggedArgs;
	}

	/**
	 * Add the appropriate tags to the command line args so that the SchedulerTaskLauncher can
	 * extract them.
	 * @param appName the name of the application to be associated with the property
	 * @param appProperties the properties to be tagged
	 * @param prefix the prefix to mark the property as to be used by the SchedulerTaskLauncher.
	 * @return the tagged command line args.
	 */
	protected Map<String, String> tagProperties(String appName, Map<String, String> appProperties, String prefix) {
		Map<String, String> taggedAppProperties = new HashMap<>(appProperties.size());

		for(String key : appProperties.keySet()) {
			if(key.contains("spring.cloud.task.name")) {
				continue;
			}
			String updatedKey = key;
			if (!key.startsWith(DATA_FLOW_URI_KEY)) {
				if (StringUtils.hasText(appName)) {
					updatedKey = this.migrateProperties.getTaskLauncherPrefix() + "." +
							prefix + appName + "." + key;
				}
				else {
					updatedKey = this.migrateProperties.getTaskLauncherPrefix() + "." +
							prefix + key;
				}
			}
			taggedAppProperties.put(updatedKey, appProperties.get(key));
		}
		return taggedAppProperties;
	}

	/**
	 * Add the required SchedulerTaskLauncher properties.
	 * @param properties the map of properties in which to add the SchedulerTaskLauncher properties.
	 * @return updated properties.
	 */
	protected Map<String, String> addSchedulerAppProps(Map<String, String> properties) {
		Map<String, String> appProperties = new HashMap<>(properties);
		appProperties.put("spring.cloud.dataflow.client.serverUri", this.migrateProperties.getDataflowServerUri());
		return appProperties;
	}
}
