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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.batch.CronJob;
import io.fabric8.kubernetes.api.model.batch.CronJobList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.dataflow.core.TaskDefinition;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleRequest;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;


/**
 * Services required to migrate schedules to the 2.5.0 format in Kubernetes
 * and stage the SchedulerTaskLauncher.
 *
 * @author Glenn Renfro
 */
public class KubernetesMigrateSchedulerService extends AbstractMigrateService {

	private static final Logger logger = LoggerFactory.getLogger(KubernetesMigrateSchedulerService.class);

	private KubernetesClient kubernetesClient;

	public KubernetesMigrateSchedulerService(TaskDefinitionRepository taskDefinitionRepository,
			AppRegistrationRepository appRegistrationRepository,
			KubernetesClient kubernetesClient, MigrateProperties migrateProperties) {
		super(migrateProperties, taskDefinitionRepository, new MavenProperties(), appRegistrationRepository);
		this.kubernetesClient = kubernetesClient;
		this.migrateProperties = migrateProperties;
	}

	/**
	 * Retrieves all the cronjobs from the kubernetes instance and extracts
	 * the schedule name, image  and command line args from each cronjob and
	 * creates a {@link ConvertScheduleInfo}.
	 * @return a {@link List} of {@link ConvertScheduleInfo}s.
	 */
	@Override
	public List<ConvertScheduleInfo> scheduleInfoList() {
		List<ConvertScheduleInfo> result = new ArrayList<>();
		CronJobList cronJobs = this.kubernetesClient.batch().cronjobs().list();
		for (CronJob cronjob : cronJobs.getItems()) {
			Container container = cronjob.getSpec().getJobTemplate().getSpec().getTemplate().getSpec().getContainers().get(0);
			ConvertScheduleInfo scheduleInfo = new ConvertScheduleInfo();
			scheduleInfo.setScheduleName(cronjob.getMetadata().getName());
			if (!isScheduleMigratable(scheduleInfo.getScheduleName())) {
				continue;
			}
			String appName = scheduleInfo.getScheduleName().substring(
					scheduleInfo.getScheduleName().indexOf(
							this.migrateProperties.getSchedulerToken()) + this.migrateProperties.getSchedulerToken().length());

			scheduleInfo = populateTaskDefinitionData(appName, scheduleInfo);

			scheduleInfo.setCommandLineArgs(container.getArgs());
			scheduleInfo.setScheduleProperties(new HashMap<>());
			scheduleInfo.getScheduleProperties().put("spring.cloud.scheduler.cron.expression", cronjob.getSpec().getSchedule());
			result.add(scheduleInfo);
		}
		Collections.sort(result);
		return result;
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

	/**
	 * Extracts the command line args from convertScheduleInfo that are tagged as properties
	 * and inserts them in the {@link ConvertScheduleInfo} app and deployer properties.
	 * Also adds the properties required for the launched task to interact with dataflow via the db.
	 * @param scheduleInfo A {@link ConvertScheduleInfo} to be enriched.
	 * @return the enriched {@link ConvertScheduleInfo}.
	 */
	@Override
	public ConvertScheduleInfo enrichScheduleMetadata(ConvertScheduleInfo scheduleInfo) {
		List<String> commandLineArgs = new ArrayList<>();
		for (String arg : scheduleInfo.getCommandLineArgs()) {
			if (arg.startsWith(this.appArgPrefix)) {
				scheduleInfo.getAppProperties().put(
						extractAppKey(arg, this.appArgPrefixLength),
						extractValue(arg));
			}
			else if (arg.startsWith(this.deployerArgPrefix)) {
				scheduleInfo.getDeployerProperties().put(
						extractDeployerKey(arg, this.deployerArgPrefixLength),
						extractValue(arg));
			}
			else if (arg.startsWith(this.commandArgPrefix)) {
				commandLineArgs.add(arg.substring(this.commandArgPrefixLength));
			}
			else {
				if (arg.startsWith("--spring.cloud.scheduler.task.launcher.taskName")) {
					continue;
				}
				commandLineArgs.add(arg);
			}
		}
		if (scheduleInfo.isCTR()) {
			scheduleInfo.getAppProperties().put("graph", scheduleInfo.getCtrDSL());
		}

		if (!scheduleInfo.getAppProperties().containsKey("spring.cloud.task.name")) {
			scheduleInfo.getAppProperties().put("spring.cloud.task.name", scheduleInfo.getTaskDefinitionName());
		}

		addDBInfoToScheduleInfo(scheduleInfo);

		if (scheduleInfo.isCTR()) {
			commandLineArgs.add("--dataflow-server-uri=" + this.migrateProperties.getDataflowUrl());
		}
		scheduleInfo.setCommandLineArgs(commandLineArgs);

		return scheduleInfo;
	}

	/**
	 * Creates a new schedule that launches the task and deletes the SchedulerTaskLauncher schedule.
	 * @param scheduler the deployer scheduler to build the new schedule.
	 * @param scheduleInfo the schedule info containing the existing schedule.
	 */
	@Override
	public void migrateSchedule(Scheduler scheduler, ConvertScheduleInfo scheduleInfo) {
		logger.info("Migrating " + scheduleInfo);
		if(scheduleInfo.getTaskResource() == null) {
			logger.info(String.format("The task definition name for schedule %s does not exist in Data Flow",
					scheduleInfo.getScheduleName()));
			return;
		}
		int scheduleTokenIndex = scheduleInfo.getScheduleName().indexOf(
				this.migrateProperties.getSchedulerToken()) - 1;
		String scheduleName = scheduleInfo.getScheduleName().substring(0, scheduleTokenIndex);
		String appName = scheduleInfo.getScheduleName().substring(
				scheduleInfo.getScheduleName().indexOf(
						this.migrateProperties.getSchedulerToken()) + this.migrateProperties.getSchedulerToken().length());
		AppDefinition appDefinition = new AppDefinition(appName, scheduleInfo.getAppProperties());
		logger.info(String.format("Extracting schedule specific properties for schedule %s", scheduleInfo.getScheduleName()));
		Map<String, String> schedulerProperties = extractAndQualifySchedulerProperties(scheduleInfo.getScheduleProperties());
		ScheduleRequest scheduleRequest = new ScheduleRequest(appDefinition, schedulerProperties, scheduleInfo.getDeployerProperties(), scheduleInfo.getCommandLineArgs(), scheduleName, scheduleInfo.getTaskResource());
		logger.info(String.format("Staging Schedule %s", scheduleName));
		scheduler.schedule(scheduleRequest);
		logger.info(String.format("Unscheduling original %s", scheduleInfo.getScheduleName()));
		scheduler.unschedule(scheduleInfo.getScheduleName());
	}

	protected Map<String, String> extractAndQualifySchedulerProperties(Map<String, String> scheduleInfoProperties) {
		scheduleInfoProperties.put("spring.cloud.scheduler.kubernetes.taskServiceAccountName", this.migrateProperties.getTaskServiceAccountName());
		//get properties from the spec
		return scheduleInfoProperties;
	}

	@Override
	public TaskDefinition findTaskDefinitionByName(String taskDefinitionName) {
		return null;
	}
}
