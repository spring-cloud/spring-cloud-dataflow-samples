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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.scheduler.SchedulerClient;
import io.pivotal.scheduler.v1.jobs.ListJobsRequest;
import io.pivotal.scheduler.v1.jobs.ListJobsResponse;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationEnvironments;
import org.cloudfoundry.operations.applications.ApplicationManifest;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.applications.GetApplicationManifestRequest;
import org.cloudfoundry.operations.applications.Route;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryConnectionProperties;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleRequest;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;
import org.springframework.cloud.deployer.spi.scheduler.SchedulerException;
import org.springframework.cloud.deployer.spi.scheduler.SchedulerPropertyKeys;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.util.StringUtils;

/**
 * Services required to migrate schedules to the 2.3.0 format in Cloud Foundry
 * and stage the SchedulerTaskLauncher.
 *
 * @author Glenn Renfro
 */
public class CFMigrateSchedulerService extends AbstractMigrateService {

	public static final String JAR_LAUNCHER = "org.springframework.boot.loader.JarLauncher";

	public static final String TASK_NAME_ARGUMENT_KEY = "--spring.cloud.scheduler.task.launcher.taskName=";

	private static final int JAR_LAUNCHER_LENGTH = JAR_LAUNCHER.length();

	private static final Logger logger = LoggerFactory.getLogger(CFMigrateSchedulerService.class);

	private static final String CLOUD_FOUNDRY_PREFIX = "cloudfoundry";

	private final static int PCF_PAGE_START_NUM = 1; //First PageNum for PCFScheduler starts at 1.

	private final static String SCHEDULER_SERVICE_ERROR_MESSAGE = "Scheduler Service returned a null response.";

	private CloudFoundryOperations cloudFoundryOperations;

	private SchedulerClient schedulerClient;

	private CloudFoundryConnectionProperties properties;

	private TaskLauncher taskLauncher;


	public CFMigrateSchedulerService(CloudFoundryOperations cloudFoundryOperations,
			SchedulerClient schedulerClient,
			CloudFoundryConnectionProperties properties, MigrateProperties migrateProperties,
			TaskDefinitionRepository taskDefinitionRepository,
			MavenProperties mavenProperties, AppRegistrationRepository appRegistrationRepository,
			TaskLauncher taskLauncher) {
		super(migrateProperties, taskDefinitionRepository, mavenProperties, appRegistrationRepository);
		this.cloudFoundryOperations = cloudFoundryOperations;
		this.schedulerClient = schedulerClient;
		this.properties = properties;
		this.taskLauncher = taskLauncher;
	}

	@Override
	public List<ConvertScheduleInfo> scheduleInfoList() {
		List<ConvertScheduleInfo> result = new ArrayList<>();
		int pageCount = getJobPageCount();
		for (int i = PCF_PAGE_START_NUM; i <= pageCount; i++) {
			logger.info(String.format("Reading Schedules Page %s of %s ", i, pageCount));
			List<ConvertScheduleInfo> scheduleInfoPage = getSchedules(i);
			if (scheduleInfoPage == null) {
				throw new SchedulerException(SCHEDULER_SERVICE_ERROR_MESSAGE);
			}
			result.addAll(scheduleInfoPage);
		}
		Collections.sort(result);
		return result;
	}

	public List<ConvertScheduleInfo> getSchedules(int page) {
		Flux<ApplicationSummary> applicationSummaries = cacheAppSummaries();
		return this.getSpace(this.properties.getSpace()).flatMap(requestSummary -> {
			return this.schedulerClient.jobs().list(ListJobsRequest.builder()
					.spaceId(requestSummary.getId())
					.page(page)
					.detailed(true).build());
		})
				.flatMapIterable(jobs -> jobs.getResources())// iterate over the resources returned.
				.flatMap(job -> {
					return getApplication(applicationSummaries,
							job.getApplicationId()) // get the application name for each job.
							.map(optionalApp -> {
								ConvertScheduleInfo scheduleInfo = new ConvertScheduleInfo();
								scheduleInfo.setScheduleProperties(new HashMap<>());
								scheduleInfo.setScheduleName(job.getName());
								scheduleInfo.setTaskDefinitionName(optionalApp.getName());
								String jobCommandLine = job.getCommand();
								String commandArgs = "";
								if (jobCommandLine != null && jobCommandLine.length() > JAR_LAUNCHER_LENGTH) {
									int locationOfArgs = job.getCommand().indexOf(JAR_LAUNCHER) + JAR_LAUNCHER_LENGTH;
									commandArgs = job.getCommand().substring(locationOfArgs);
								}
								else {
									logger.warn(String.format("Job %s does not have commandArgs associated with it.", job.getName()));
								}
								if (StringUtils.hasText(commandArgs)) {
									try {
										scheduleInfo.setCommandLineArgs(Arrays.asList(CommandLineUtils.translateCommandline(commandArgs)));
									}
									catch (Exception e) {
										throw new IllegalArgumentException(e);
									}
								}
								if (job.getJobSchedules() != null) {
									scheduleInfo.getScheduleProperties().put(SchedulerPropertyKeys.CRON_EXPRESSION,
											job.getJobSchedules().get(0).getExpression());
								}
								else {
									logger.warn(String.format("Job %s does not have an associated schedule", job.getName()));
								}
								return scheduleInfo;
							});
				})
				.filter(job -> isScheduleMigratable(job.getScheduleName()))
				.collectList().block();
	}

	private boolean isScheduleMigratable(String scheduleName) {
		boolean result;
		if (this.migrateProperties.getScheduleNamesToMigrate().size() > 0) {
			result = this.migrateProperties.getScheduleNamesToMigrate().contains(scheduleName);
		}
		else {
			result = true;
		}
		return result;
	}

	@Override
	public ConvertScheduleInfo enrichScheduleMetadata(ConvertScheduleInfo scheduleInfo) {
		logger.info(String.format("Retrieving Properties from application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));
		ApplicationEnvironments environment = this.cloudFoundryOperations.applications().
				getEnvironments(GetApplicationEnvironmentsRequest.builder().
						name(scheduleInfo.getTaskDefinitionName()).
						build()).
				block();

		logger.info(String.format("Retrieving ApplicationManifest for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));
		ApplicationManifest applicationManifest = getApplicationManifest(scheduleInfo.getTaskDefinitionName());
		if (applicationManifest != null) {
			addApplicationManifestPropsToConvertScheduleInfo(scheduleInfo, applicationManifest);
		}
		if (environment != null) {
			for (Map.Entry<String, Object> var : environment.getUserProvided().entrySet()) {
				scheduleInfo.getScheduleProperties().put(var.getKey(), (String) var.getValue());
			}
		}
		logger.info(String.format("Tagging command line args for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));

		String appName = getAppNameFromArgs(scheduleInfo);

		Map<String, String> appProperties;
		try {
			logger.info(String.format("Extracting Spring App Properties for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));
			appProperties = getSpringAppProperties(scheduleInfo.getScheduleProperties());
			if (appProperties.size() > 0) {
				scheduleInfo.setUseSpringApplicationJson(true);
			}
		}
		catch (Exception exception) {
			throw new IllegalArgumentException("Unable to parse SPRING_APPLICATION_JSON from USER VARIABLES", exception);
		}
		populateTaskDefinitionData(appName, scheduleInfo);

		appProperties = cleanseAppProperties(scheduleInfo, appProperties);
		if (scheduleInfo.isCTR()) {
			appProperties.put("graph", scheduleInfo.getCtrDSL());
		}

		logger.info(String.format("Retrieving Task Definition for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));

		logger.info(String.format("Tagging app properties for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));
		Map<String, String> deployerProperties = getDeployerProperties(scheduleInfo);
		appProperties.put("dataflow-server-uri", this.migrateProperties.getDataflowUrl());
		appProperties.putAll(deployerProperties);
		scheduleInfo.setAppProperties(appProperties);

		scheduleInfo.setCommandLineArgs(cleanseCommandLineArgs(scheduleInfo, appName));
		addDBInfoToScheduleInfo(scheduleInfo);
		return scheduleInfo;
	}

	@Override
	public void migrateSchedule(Scheduler scheduler, ConvertScheduleInfo scheduleInfo) {
		logger.info("Migrating " + scheduleInfo);
		if (scheduleInfo.getTaskResource() == null) {
			logger.info(String.format("The task definition name for schedule %s does not exist in Data Flow",
					scheduleInfo.getScheduleName()));
			return;
		}
		String scheduleName = scheduleInfo.getScheduleName().substring(0,
				scheduleInfo.getScheduleName().indexOf(this.migrateProperties.getSchedulerToken()) - 1);
		AppDefinition appDefinition = new AppDefinition(scheduleInfo.getTaskDefinitionName(), scheduleInfo.getAppProperties());
		logger.info(String.format("Extracting schedule specific properties for schedule %s", scheduleInfo.getScheduleName()));
		Map<String, String> schedulerProperties = extractAndQualifySchedulerProperties(scheduleInfo.getScheduleProperties());
		ScheduleRequest scheduleRequest = new ScheduleRequest(appDefinition, schedulerProperties, new HashMap<>(), scheduleInfo.getCommandLineArgs(), scheduleName, scheduleInfo.getTaskResource());
		logger.info(String.format("Staging ScheduleTaskLauncher and scheduling %s", scheduleInfo.getScheduleName()));

		scheduler.schedule(scheduleRequest);
		logger.info(String.format("Unscheduling original %s", scheduleInfo.getScheduleName()));
		this.taskLauncher.destroy(scheduleInfo.getScheduleName());
	}

	/**
	 * Retrieves the number of pages that can be returned when retrieving a list of jobs.
	 *
	 * @return an int containing the number of available pages.
	 */
	private int getJobPageCount() {
		ListJobsResponse response = this.getSpace(this.properties.getSpace()).flatMap(requestSummary -> {
			return this.schedulerClient.jobs().list(ListJobsRequest.builder()
					.spaceId(requestSummary.getId())
					.detailed(false).build());
		}).block();
		if (response == null) {
			throw new SchedulerException(SCHEDULER_SERVICE_ERROR_MESSAGE);
		}
		return response.getPagination().getTotalPages();
	}

	private Map<String, String> getSpringAppProperties(Map<String, String> properties) throws Exception {
		Map<String, String> result = new HashMap<>();
		if (properties.containsKey("SPRING_APPLICATION_JSON")) {
			result = new ObjectMapper()
					.readValue(properties.get("SPRING_APPLICATION_JSON"), Map.class);
		}
		return result;
	}

	/**
	 * Retrieve a {@link Mono} containing a {@link SpaceSummary} for the specified name.
	 *
	 * @param spaceName the name of space to search.
	 * @return the {@link SpaceSummary} associated with the spaceName.
	 */
	private Mono<SpaceSummary> getSpace(String spaceName) {
		return requestSpaces()
				.cache() //cache results from first call.
				.filter(space -> spaceName.equals(space.getName()))
				.singleOrEmpty()
				.cast(SpaceSummary.class);
	}

	/**
	 * Retrieve a {@link Flux} containing the available {@link SpaceSummary}s.
	 *
	 * @return {@link Flux} of {@link SpaceSummary}s.
	 */
	private Flux<SpaceSummary> requestSpaces() {
		return this.cloudFoundryOperations.spaces()
				.list();
	}

	/**
	 * Retrieve a cached {@link Flux} of {@link ApplicationSummary}s.
	 */
	private Flux<ApplicationSummary> cacheAppSummaries() {
		return requestListApplications()
				.cache(); //cache results from first call.  No need to re-retrieve each time.
	}

	/**
	 * Retrieve a  {@link Flux} of {@link ApplicationSummary}s.
	 */
	private Flux<ApplicationSummary> requestListApplications() {
		return this.cloudFoundryOperations.applications()
				.list();
	}

	/**
	 * Retrieve a {@link Mono} containing the {@link ApplicationSummary} associated with the appId.
	 *
	 * @param applicationSummaries {@link Flux} of {@link ApplicationSummary}s to filter.
	 * @param appId                the id of the {@link ApplicationSummary} to search.
	 */
	private Mono<ApplicationSummary> getApplication(Flux<ApplicationSummary> applicationSummaries,
			String appId) {
		return applicationSummaries
				.filter(application -> appId.equals(application.getId()))
				.singleOrEmpty();
	}

	private ApplicationManifest getApplicationManifest(String appName) {
		return this.cloudFoundryOperations.applications()
				.getApplicationManifest(GetApplicationManifestRequest
						.builder().name(appName).build())
				.block();
	}

	private Map<String, String> getDeployerProperties(ConvertScheduleInfo scheduleInfo) {
		Map<String, String> result = new HashMap<>();
		if (scheduleInfo.getJavaBuildPack() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".buildpack", scheduleInfo.getJavaBuildPack());
		}
		if (scheduleInfo.getMemoryInMB() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".memory", scheduleInfo.getMemoryInMB() + "m");
		}
		if (scheduleInfo.getDiskInMB() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".disk", scheduleInfo.getDiskInMB() + "m");
		}
		if (scheduleInfo.getApplicationHealthCheck() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".health-check", scheduleInfo.getApplicationHealthCheck().getValue());
		}
		if (scheduleInfo.getHealthCheckEndPoint() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".health-check-http-endpoint", scheduleInfo.getHealthCheckEndPoint());
		}
		if (scheduleInfo.getServices() != null && scheduleInfo.getServices().size() > 0) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".services", StringUtils.arrayToCommaDelimitedString(scheduleInfo.getServices().toArray()));
		}
		if (scheduleInfo.getDomains() != null && scheduleInfo.getDomains().size() > 0) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".domain", StringUtils.arrayToCommaDelimitedString(scheduleInfo.getDomains().toArray()));
		}
		if (scheduleInfo.getRoutes() != null && scheduleInfo.getRoutes().size() > 0) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".route-path", StringUtils.arrayToCommaDelimitedString(scheduleInfo.getRoutes().toArray()));
		}
		if (scheduleInfo.getHosts() != null && scheduleInfo.getHosts().size() > 0) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".host", StringUtils.arrayToCommaDelimitedString(scheduleInfo.getHosts().toArray()));
		}

		return result;
	}

	private void addApplicationManifestPropsToConvertScheduleInfo(ConvertScheduleInfo scheduleInfo, ApplicationManifest applicationManifest) {
		scheduleInfo.setDiskInMB(applicationManifest.getDisk());
		scheduleInfo.setMemoryInMB(applicationManifest.getMemory());
		scheduleInfo.setApplicationHealthCheck(applicationManifest.getHealthCheckType());
		scheduleInfo.setJavaBuildPack(applicationManifest.getBuildpack());
		scheduleInfo.setHealthCheckEndPoint(applicationManifest.getHealthCheckHttpEndpoint());
		if (applicationManifest.getServices() != null && applicationManifest.getServices().size() > 0) {
			scheduleInfo.setServices(applicationManifest.getServices());
		}
		if (applicationManifest.getDomains() != null && applicationManifest.getDomains().size() > 0) {
			scheduleInfo.setDomains(applicationManifest.getDomains());
		}
		if (applicationManifest.getRoutes() != null && applicationManifest.getRoutes().size() > 0) {
			List<String> routes = new ArrayList<>();
			for (Route route : applicationManifest.getRoutes()) {
				routes.add(route.getRoute());
			}
			scheduleInfo.setRoutes(routes);
		}
		if (applicationManifest.getHosts() != null && applicationManifest.getHosts().size() > 0) {
			List<String> hosts = new ArrayList<>(applicationManifest.getHosts());
			scheduleInfo.setHosts(hosts);
		}
	}

	private List<String> cleanseCommandLineArgs(ConvertScheduleInfo scheduleInfo, String appName) {
		List<String> commandLineArgs = new ArrayList<>();

		for (String arg : scheduleInfo.getCommandLineArgs()) {
			String resultArg = (arg.startsWith("--")) ? "--" : "";
			if (arg.startsWith(COMMAND_ARGUMENT_PREFIX)) {
				commandLineArgs.add(resultArg + extractAppKey(arg, this.appArgPrefixLength) + "=" + extractValue(arg));
			}
			else if (arg.startsWith(this.deployerArgPrefix)) {
				commandLineArgs.add(resultArg + extractDeployerKey(arg, this.appArgPrefixLength) + "=" + extractValue(arg));
			}
			else if (arg.startsWith(this.commandArgPrefix)) {
				commandLineArgs.add("--" + arg.substring(this.commandArgPrefixLength));
			}
			else {
				if (arg.startsWith("--spring.cloud.scheduler.task.launcher.taskName")) {
					continue;
				}
				commandLineArgs.add(arg);
			}
		}
		commandLineArgs.add("--spring.application.name=" + appName);
		return commandLineArgs;
	}

	private String cleanseAppPropertyKey(String key) {
		String prefix = String.format("%s.%s", this.migrateProperties.getTaskLauncherPrefix(), APP_PREFIX);
		key = key.substring(prefix.length());
		int dotIndex = key.indexOf(".");
		String result = key;
		if (!key.substring(0, dotIndex).equals("management")) {
			result = key.substring(dotIndex + 1);
		}
		return result;
	}

	private Map<String, String> cleanseAppProperties(ConvertScheduleInfo scheduleInfo, Map<String, String> properties) {
		Map<String, String> result = new HashMap<>();
		String ctrProperties = "";
		boolean isFirstCTREntry = true;
		for (String key : properties.keySet()) {
			if (scheduleInfo.isCTR()) {
				String ctrPropertyCandidate = prepScheduleForCTR(scheduleInfo, key, properties.get(key));
				if (ctrPropertyCandidate != null) {
					if (isFirstCTREntry) {
						isFirstCTREntry = false;
						ctrProperties += ctrPropertyCandidate;
					}
					else {
						ctrProperties = ctrProperties + "," + ctrPropertyCandidate;
					}
					continue;
				}
			}
			if (key.startsWith(this.migrateProperties.getTaskLauncherPrefix())) {
				String cleansedKey = cleanseAppPropertyKey(key);
				result.put(cleansedKey, properties.get(key));
			}
			else {
				result.put(key, properties.get(key));
			}
		}
		if (!ctrProperties.isEmpty()) {
			result.put("composed-task-properties", ctrProperties);
		}
		return result;
	}

	private String prepScheduleForCTR(ConvertScheduleInfo scheduleInfo, String key, String value) {
		String ctrProperty = null;
		String prefix = String.format("%s.%s%s.", this.migrateProperties.getTaskLauncherPrefix(), APP_PREFIX, scheduleInfo.getTaskDefinitionName());
		if (key.startsWith(prefix)) {
			String newKey = key.substring(prefix.length());
			String appName = newKey.substring(0, newKey.indexOf("."));
			ctrProperty = String.format("%s%s-%s.%s%s=%s", APP_PREFIX,
					scheduleInfo.getTaskDefinitionName(), appName, APP_PREFIX,
					newKey, value);
		}
		return ctrProperty;
	}

	private String getAppNameFromArgs(ConvertScheduleInfo scheduleInfo) {
		String appName = null;
		for (String command : scheduleInfo.getCommandLineArgs()) {
			if (command.startsWith(TASK_NAME_ARGUMENT_KEY)) {
				int appNameIndex = command.indexOf(TASK_NAME_ARGUMENT_KEY);
				appName = command.substring(appNameIndex + TASK_NAME_ARGUMENT_KEY.length());
				break;
			}
		}
		return appName;
	}
}
