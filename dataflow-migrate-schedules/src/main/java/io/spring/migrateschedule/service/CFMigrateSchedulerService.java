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

import org.springframework.cloud.dataflow.core.TaskDefinition;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryConnectionProperties;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleRequest;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;
import org.springframework.cloud.deployer.spi.scheduler.SchedulerException;
import org.springframework.cloud.deployer.spi.scheduler.SchedulerPropertyKeys;
import org.springframework.util.StringUtils;

/**
 * Services required to migrate schedules to the 2.3.0 format in Cloud Foundry
 * and stage the ShedulerTaskLauncher.
 *
 * @author Glenn Renfro
 */
public class CFMigrateSchedulerService extends AbstractMigrateService {

	private static final Logger logger = LoggerFactory.getLogger(CFMigrateSchedulerService.class);

	private static final String CLOUD_FOUNDRY_PREFIX = "cloudfoundry";

	private final static int PCF_PAGE_START_NUM = 1; //First PageNum for PCFScheduler starts at 1.

	private final static String SCHEDULER_SERVICE_ERROR_MESSAGE = "Scheduler Service returned a null response.";

	private CloudFoundryOperations cloudFoundryOperations;

	private SchedulerClient schedulerClient;

	private CloudFoundryConnectionProperties properties;


	public CFMigrateSchedulerService(CloudFoundryOperations cloudFoundryOperations,
			SchedulerClient schedulerClient,
			CloudFoundryConnectionProperties properties, MigrateProperties migrateProperties,
			TaskDefinitionRepository taskDefinitionRepository, MavenProperties mavenProperties) {
		super(migrateProperties, taskDefinitionRepository, mavenProperties);
		this.cloudFoundryOperations = cloudFoundryOperations;
		this.schedulerClient = schedulerClient;
		this.properties = properties;
	}

	@Override
	public List<ConvertScheduleInfo> scheduleInfoList() {
		List<ConvertScheduleInfo> result = new ArrayList<>();
		int pageCount = getJobPageCount();
		for (int i = PCF_PAGE_START_NUM; i <= pageCount; i++) {
			logger.info(String.format("Reading Schedules Page %s of %s ", i, pageCount ));
			List<ConvertScheduleInfo> scheduleInfoPage = getSchedules(i);
			if(scheduleInfoPage == null) {
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

								int locationOfArgs = job.getCommand().indexOf("org.springframework.boot.loader.JarLauncher") + "org.springframework.boot.loader.JarLauncher".length();
								String commandArgs = job.getCommand().substring(locationOfArgs);
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
		if(migrateProperties.getScheduleNamesToMigrate().size() > 0) {
			result = migrateProperties.getScheduleNamesToMigrate().contains(scheduleName);
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
		if(applicationManifest != null) {
			addApplicationManifestPropsToConvertScheduleInfo(scheduleInfo, applicationManifest);
		}
		if (environment != null) {
			for (Map.Entry<String, Object> var : environment.getUserProvided().entrySet()) {
				scheduleInfo.getScheduleProperties().put(var.getKey(), (String) var.getValue());
			}
		}
		logger.info(String.format("Tagging command line args for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));
		List<String> revisedCommandLineArgs = tagCommandLineArgs(scheduleInfo.getCommandLineArgs());
		revisedCommandLineArgs.add("--spring.cloud.scheduler.task.launcher.taskName=" + scheduleInfo.getTaskDefinitionName());
		scheduleInfo.setCommandLineArgs(revisedCommandLineArgs);
		Map<String, String> appProperties = null;
		try {
			logger.info(String.format("Extracting Spring App Properties for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));
			appProperties = getSpringAppProperties(scheduleInfo.getScheduleProperties());
			if(appProperties.size() > 0) {
				scheduleInfo.setUseSpringApplicationJson(true);
			}
		}
		catch (Exception exception) {
			throw new IllegalArgumentException("Unable to parse SPRING_APPLICATION_JSON from USER VARIABLES", exception);
		}
		logger.info(String.format("Retrieving Task Definition for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));
		TaskDefinition taskDefinition = findTaskDefinitionByName(appProperties.get("spring.cloud.task.name"));
		if (appProperties.size() > 0 && taskDefinition == null) {
			throw new IllegalStateException(String.format("The schedule %s contains " +
							"properties but the task definition %s does not exist and thus can't be migrated",
					scheduleInfo.getScheduleName(), scheduleInfo.getTaskDefinitionName()));
		}
		logger.info(String.format("Tagging app properties for application %s for schedule %s", scheduleInfo.getTaskDefinitionName(), scheduleInfo.getScheduleName()));
		appProperties = tagProperties(taskDefinition.getRegisteredAppName(), appProperties, APP_PREFIX);
		Map<String, String> deployerProperties = tagProperties(taskDefinition.getRegisteredAppName(),
				getDeployerProperties(scheduleInfo), DEPLOYER_PREFIX);
		appProperties = addSchedulerAppProps(appProperties);
		appProperties.putAll(deployerProperties);
		scheduleInfo.setAppProperties(appProperties);
		return scheduleInfo;
	}

	@Override
	public void migrateSchedule(Scheduler scheduler, ConvertScheduleInfo scheduleInfo) {
		String scheduleName = scheduleInfo.getScheduleName() + "-" + getSchedulePrefixDefinitionName(scheduleInfo.getTaskDefinitionName());
		AppDefinition appDefinition = new AppDefinition(scheduleName, scheduleInfo.getAppProperties());
		logger.info(String.format("Extracting schedule specific properties for schedule %s", scheduleInfo.getScheduleName()));
		Map<String, String> schedulerProperties = extractAndQualifySchedulerProperties(scheduleInfo.getScheduleProperties());
		ScheduleRequest scheduleRequest = new ScheduleRequest(appDefinition, schedulerProperties, new HashMap<>(), scheduleInfo.getCommandLineArgs(), scheduleName, getTaskLauncherResource());
		logger.info(String.format("Staging ScheduleTaskLauncher and scheduling %s", scheduleInfo.getScheduleName()));
		scheduler.schedule(scheduleRequest);
		logger.info(String.format("Unscheduling original %s", scheduleInfo.getScheduleName()));
		scheduler.unschedule(scheduleInfo.getScheduleName());
	}

	/**
	 * Retrieves the number of pages that can be returned when retrieving a list of jobs.
	 * @return an int containing the number of available pages.
	 */
	private int getJobPageCount() {
		ListJobsResponse response = this.getSpace(this.properties.getSpace()).flatMap(requestSummary -> {
			return this.schedulerClient.jobs().list(ListJobsRequest.builder()
					.spaceId(requestSummary.getId())
					.detailed(false).build());
		}).block();
		if(response == null) {
			throw new SchedulerException(SCHEDULER_SERVICE_ERROR_MESSAGE);
		}
		return response.getPagination().getTotalPages();
	}

	private Map<String, String> getSpringAppProperties(Map<String, String> properties) throws Exception {
		Map<String, String> result;
		if(properties.containsKey("SPRING_APPLICATION_JSON")) {
			result = new ObjectMapper()
					.readValue(properties.get("SPRING_APPLICATION_JSON"), Map.class);
		}
		else {
			result = new HashMap<>();
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
			result.put(CLOUD_FOUNDRY_PREFIX +  ".memory", scheduleInfo.getMemoryInMB() + "m");
		}
		if (scheduleInfo.getDiskInMB() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX +  ".disk", scheduleInfo.getDiskInMB() + "m");
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

		// Global deployer properties;
		if (this.migrateProperties.getHealthCheckTimeout() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".health-check-timeout", this.migrateProperties.getHealthCheckTimeout());
		}
		if (this.migrateProperties.getJavaOptions() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".javaOpts", this.migrateProperties.getJavaOptions());
		}
		if (this.migrateProperties.getApiTimeout() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".api-timeout", String.valueOf(this.migrateProperties.getApiTimeout()));
		}
		if (this.migrateProperties.getStatusTimeout() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".status-timeout", String.valueOf(this.migrateProperties.getStatusTimeout()));
		}
		if (this.migrateProperties.getStagingTimeout() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".staging-timeout", String.valueOf(this.migrateProperties.getStagingTimeout()));
		}
		if (this.migrateProperties.getStartupTimeout() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".startup-timeout", String.valueOf(this.migrateProperties.getStartupTimeout()));
		}
		if (this.migrateProperties.getMaximumConcurrentTasks() != null) {
			result.put(CLOUD_FOUNDRY_PREFIX + ".maximum-concurrent-tasks", String.valueOf(this.migrateProperties.getMaximumConcurrentTasks()));
		}

		return result;
	}

	private ConvertScheduleInfo addApplicationManifestPropsToConvertScheduleInfo(ConvertScheduleInfo scheduleInfo, ApplicationManifest applicationManifest) {
		scheduleInfo.setDiskInMB(applicationManifest.getDisk());
		scheduleInfo.setMemoryInMB(applicationManifest.getMemory());
		scheduleInfo.setApplicationHealthCheck(applicationManifest.getHealthCheckType());
		scheduleInfo.setJavaBuildPack(applicationManifest.getBuildpack());
		scheduleInfo.setHealthCheckEndPoint(applicationManifest.getHealthCheckHttpEndpoint());
		if(applicationManifest.getServices() != null && applicationManifest.getServices().size() > 0) {
			scheduleInfo.setServices(applicationManifest.getServices());
		}
		if(applicationManifest.getDomains() != null && applicationManifest.getDomains().size() > 0) {
			scheduleInfo.setDomains(applicationManifest.getDomains());
		}
		if(applicationManifest.getRoutes() != null && applicationManifest.getRoutes().size() > 0) {
			List<String> routes = new ArrayList<>();
			for (Route route : applicationManifest.getRoutes()) {
				routes.add(route.getRoute());
			}
			scheduleInfo.setRoutes(routes);
		}
		if (applicationManifest.getHosts() != null && applicationManifest.getHosts().size() > 0) {
			List<String> hosts = new ArrayList<>();
			for (String host : applicationManifest.getHosts()) {
				hosts.add(host);
			}
			scheduleInfo.setHosts(hosts);
		}

		return scheduleInfo;
	}
}
