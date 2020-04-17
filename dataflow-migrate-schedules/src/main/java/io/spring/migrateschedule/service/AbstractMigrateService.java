/*
 * Copyright 2020 the original author or authors.
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
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.cloud.dataflow.core.AppRegistration;
import org.springframework.cloud.dataflow.core.ApplicationType;
import org.springframework.cloud.dataflow.core.TaskDefinition;
import org.springframework.cloud.dataflow.core.dsl.TaskNode;
import org.springframework.cloud.dataflow.core.dsl.TaskParser;
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

	public final static String COMMAND_ARGUMENT_PREFIX = "cmdarg.";

	public final static String APP_PREFIX = "app.";

	public final static String DEPLOYER_PREFIX = "deployer.";

	protected MigrateProperties migrateProperties;

	protected TaskDefinitionRepository taskDefinitionRepository;

	protected AppRegistrationRepository appRegistrationRepository;

	protected MavenProperties mavenProperties;

	protected final String appArgPrefix;

	protected final String deployerArgPrefix;

	protected final String schedulerArgAppPrefix;

	protected final String commandArgPrefix;

	protected final int appArgPrefixLength;

	protected final int deployerArgPrefixLength;

	protected final int commandArgPrefixLength;

	public AbstractMigrateService(MigrateProperties migrateProperties,
			TaskDefinitionRepository taskDefinitionRepository,
			MavenProperties mavenProperties, AppRegistrationRepository appRegistrationRepository) {
		this.migrateProperties = migrateProperties;
		this.taskDefinitionRepository = taskDefinitionRepository;
		this.mavenProperties = mavenProperties;
		this.appRegistrationRepository = appRegistrationRepository;
		this.schedulerArgAppPrefix = String.format("--%s.", this.migrateProperties.getTaskLauncherPrefix());
		this.appArgPrefix = String.format("%s%s", this.schedulerArgAppPrefix, APP_PREFIX);
		this.deployerArgPrefix = String.format("%s%s", this.schedulerArgAppPrefix, DEPLOYER_PREFIX);
		this.appArgPrefixLength = this.appArgPrefix.length();
		this.deployerArgPrefixLength = this.deployerArgPrefix.length();
		this.commandArgPrefix = String.format("%s%s.", COMMAND_ARGUMENT_PREFIX, this.migrateProperties.getTaskLauncherPrefix());
		this.commandArgPrefixLength = this.commandArgPrefix.length();
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
	protected Map<String, String> extractAndQualifySchedulerProperties(Map<String, String> input) {
		final String prefix = "spring.cloud.scheduler.";

		Map<String, String> result = new TreeMap<>(input).entrySet().stream()
				.filter(kv -> kv.getKey().startsWith(prefix))
				.collect(Collectors.toMap(kv -> kv.getKey(), kv -> kv.getValue(),
						(fromWildcard, fromApp) -> fromApp));

		return result;
	}

	/**
	 * Retrieve the resource for the Scheduled task
	 * @return {@link Resource} for the scheduled task.
	 */
	protected Resource getTaskLauncherResource(String resource) {
		try {
			new URI(resource); //verify url
		}
		catch (URISyntaxException uriSyntaxException) {
			throw new IllegalArgumentException(uriSyntaxException);
		}
		AppResourceCommon appResourceCommon = new AppResourceCommon(this.mavenProperties, new DefaultResourceLoader());
		return appResourceCommon.getResource(resource);
	}

	protected String extractDeployerKey(String arg, int deployerPrefixLength) {
		return "spring.cloud.deployer." + extractAppKey(arg, deployerPrefixLength);
	}

	protected String extractAppKey(String arg, int prefixLength) {
		int indexOfEqual = arg.indexOf("=");
		arg = arg.substring(prefixLength, indexOfEqual);
		int dotIndex = arg.indexOf(".");
		String result = arg;
		if (!arg.substring(0, dotIndex).equals("management")) {
			result = arg.substring(dotIndex + 1);
		}
		return result;
	}


	protected String extractValue(String arg) {
		int indexOfEqual = arg.indexOf("=");
		return arg.substring(indexOfEqual + 1);
	}

	protected ConvertScheduleInfo populateTaskDefinitionData(String appName, ConvertScheduleInfo scheduleInfo) {
		TaskDefinition taskDefinition = this.taskDefinitionRepository.findByTaskName(appName);

		if (taskDefinition != null) {
			TaskParser taskParser = new TaskParser(appName, taskDefinition.getDslText(), false, false);
			String registeredAppName = taskDefinition.getRegisteredAppName();
			TaskNode taskNode = taskParser.parse();
			if (taskNode.isComposed()) {
				registeredAppName = this.migrateProperties.getComposedTaskRunnerRegisteredAppName();
				scheduleInfo.setCtrDSL(taskNode.toExecutableDSL());
				scheduleInfo.setCTR(true);
			}

			AppRegistration appRegistration = this.appRegistrationRepository.findAppRegistrationByNameAndTypeAndDefaultVersionIsTrue(registeredAppName, ApplicationType.task);

			scheduleInfo.setTaskResource(getTaskLauncherResource(appRegistration.getUri().toString()));
			scheduleInfo.setTaskDefinitionName(appName);
		}
		return scheduleInfo;
	}

	protected ConvertScheduleInfo addDBInfoToScheduleInfo(ConvertScheduleInfo scheduleInfo) {
		if(StringUtils.hasText(this.migrateProperties.getDbUrl())) {
			scheduleInfo.getAppProperties().put("spring.datasource.url", this.migrateProperties.getDbUrl());
		}
		if(StringUtils.hasText(this.migrateProperties.getDbDriverClassName())) {
			scheduleInfo.getAppProperties().put("spring.datasource.driverClassName", this.migrateProperties.getDbDriverClassName());
		}
		if(StringUtils.hasText(this.migrateProperties.getDbUserName())) {
			scheduleInfo.getAppProperties().put("spring.datasource.username", this.migrateProperties.getDbUserName());
		}
		if(StringUtils.hasText(this.migrateProperties.getDbPassword())) {
			scheduleInfo.getAppProperties().put("spring.datasource.password", this.migrateProperties.getDbPassword());
		}
		return scheduleInfo;
	}
}
