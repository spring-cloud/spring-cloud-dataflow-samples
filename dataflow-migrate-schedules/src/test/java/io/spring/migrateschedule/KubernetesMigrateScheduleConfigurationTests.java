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

package io.spring.migrateschedule;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.batch.CronJob;
import io.fabric8.kubernetes.api.model.batch.CronJobList;
import io.fabric8.kubernetes.api.model.batch.CronJobSpec;
import io.fabric8.kubernetes.api.model.batch.DoneableCronJob;
import io.fabric8.kubernetes.api.model.batch.JobSpec;
import io.fabric8.kubernetes.api.model.batch.JobTemplateSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.BatchAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.spring.migrateschedule.service.AppRegistrationRepository;
import io.spring.migrateschedule.service.ConvertScheduleInfo;
import io.spring.migrateschedule.service.KubernetesMigrateSchedulerService;
import io.spring.migrateschedule.service.MigrateProperties;
import io.spring.migrateschedule.service.TaskDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import org.springframework.cloud.dataflow.core.AppRegistration;
import org.springframework.cloud.dataflow.core.TaskDefinition;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleRequest;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class KubernetesMigrateScheduleConfigurationTests {

	public static final String DEFAULT_SCHEDULE_NAME = "defaultScheduleName-scdf-mydef";
	public static final String DEFAULT_TASK_DEFINITION_NAME = "defaultTaskDefinitionName";
	public static final String DEFAULT_APP_NAME = "defaultAppName";
	public static final String DEFAULT_CMD_ARG = "defaultCmd=WOW";
	public static final String DEFAULT_BUILD_PACK = "defaultBuildPack";
	public static final String DEFAULT_DB_URL = "jdbc:mydb://localhost:8888/task";
	public static final String DEFAULT_DRIVER_CLASS_NAME = "myDriverClassName";
	public static final String DEFAULT_DB_USER_NAME = "myuser";
	public static final String DEFAULT_DB_PASSWORD = "mypassword";

	private KubernetesMigrateSchedulerService k8MigrateSchedulerService;
	private KubernetesClient kubernetesClient;
	private MigrateProperties migrateProperties;
	private TaskDefinitionRepository taskDefinitionRepository;
	private Scheduler scheduler;
	private AppRegistrationRepository appRegistrationRepository;
	private List<CronJob> sampleCronJobs;

	@BeforeEach
		public void setup() {
		this.kubernetesClient = Mockito.mock(KubernetesClient.class);
		this.migrateProperties = new MigrateProperties();
		this.taskDefinitionRepository = Mockito.mock(TaskDefinitionRepository.class);
		this.scheduler = Mockito.mock(Scheduler.class);
		this.appRegistrationRepository = Mockito.mock(AppRegistrationRepository.class);
		this.migrateProperties.setDbUrl(DEFAULT_DB_URL);
		this.migrateProperties.setDbDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
		this.migrateProperties.setDbUserName(DEFAULT_DB_USER_NAME);
		this.migrateProperties.setDbPassword(DEFAULT_DB_PASSWORD);

		this.k8MigrateSchedulerService = new KubernetesMigrateSchedulerService(this.taskDefinitionRepository,
				this.appRegistrationRepository,
				this.kubernetesClient, this.migrateProperties) ;

		BatchAPIGroupDSL batchAPIGroupDSL = Mockito.mock(BatchAPIGroupDSL.class);
		MixedOperation<CronJob, CronJobList, DoneableCronJob, io.fabric8.kubernetes.client.dsl.Resource<CronJob, DoneableCronJob>> cronJobs = Mockito.mock(MixedOperation.class);
		CronJobList cronJobList = Mockito.mock(CronJobList.class);
		Mockito.when(this.kubernetesClient.batch()).thenReturn(batchAPIGroupDSL);
		Mockito.when(batchAPIGroupDSL.cronjobs()).thenReturn(cronJobs);
		Mockito.when(cronJobs.list()).thenReturn(cronJobList);
		sampleCronJobs = new ArrayList<>();
		Mockito.when(cronJobList.getItems()).thenReturn(sampleCronJobs);

		//Setup kubernetesClient Mock to return a single schedule item in the result
		CronJob cronJob = new CronJob();
		ObjectMeta objectMeta = new ObjectMeta();
		objectMeta.setName(DEFAULT_SCHEDULE_NAME);
		cronJob.setMetadata(objectMeta);
		this.sampleCronJobs.add(cronJob);
		CronJobSpec cronJobSpec = new CronJobSpec();
		cronJob.setSpec(cronJobSpec);
		JobTemplateSpec jobTemplateSpec = new JobTemplateSpec();
		cronJobSpec.setJobTemplate(jobTemplateSpec);
		JobSpec jobSpec = new JobSpec();
		jobTemplateSpec.setSpec(jobSpec);
		PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
		jobSpec.setTemplate(podTemplateSpec);
		PodSpec podSpec = new PodSpec();
		podTemplateSpec.setSpec(podSpec);

		Container container = new Container();
		podSpec.getContainers().add(container);

	}

	@Test
	public void testEnrichment() {
		ConvertScheduleInfo scheduleInfo = createFoundationConvertScheduleInfo();
		assertThat(scheduleInfo.getAppProperties().keySet().size()).isEqualTo(6);
		assertThat(scheduleInfo.getAppProperties().get("foo")).isEqualTo("bar");

		validateDBProperties(scheduleInfo);
		assertThat(scheduleInfo.getAppProperties().get("spring.cloud.task.name")).isEqualTo(DEFAULT_TASK_DEFINITION_NAME);

		assertThat(scheduleInfo.getCommandLineArgs().size()).isEqualTo(1);
		assertThat(scheduleInfo.getCommandLineArgs().get(0)).isEqualTo("defaultCmd=WOW");
	}

	@Test
	public void testEnrichmentNoProps() {
		ConvertScheduleInfo scheduleInfo = new ConvertScheduleInfo();
		scheduleInfo.setScheduleName(DEFAULT_SCHEDULE_NAME);
		scheduleInfo.setTaskDefinitionName(DEFAULT_TASK_DEFINITION_NAME);
		scheduleInfo.setScheduleProperties(new HashMap<>());
		scheduleInfo.setRegisteredAppName(DEFAULT_APP_NAME);
		TaskDefinition taskDefinition = TaskDefinition.TaskDefinitionBuilder
				.from(new TaskDefinition("fooTask", "foo"))
				.setTaskName(DEFAULT_TASK_DEFINITION_NAME)
				.setRegisteredAppName(DEFAULT_APP_NAME)
				.build();
		Mockito.when(this.taskDefinitionRepository.findByTaskName(Mockito.any())).thenReturn(taskDefinition);
		scheduleInfo = this.k8MigrateSchedulerService.enrichScheduleMetadata(scheduleInfo);
		assertThat(scheduleInfo.getAppProperties().keySet().size()).isEqualTo(5);
		validateDBProperties(scheduleInfo);
		assertThat(scheduleInfo.getAppProperties().get("spring.cloud.task.name")).isEqualTo(DEFAULT_TASK_DEFINITION_NAME);
		assertThat(scheduleInfo.getCommandLineArgs().size()).isEqualTo(0);
	}

	@Test
	public void testListSchedules() {

		TaskDefinition taskDefinition = TaskDefinition.TaskDefinitionBuilder
				.from(new TaskDefinition("fooTask", "foo"))
				.setTaskName(DEFAULT_TASK_DEFINITION_NAME)
				.setRegisteredAppName(DEFAULT_APP_NAME)
				.setDslText("foo")
				.build();
		AppRegistration appRegistration = new AppRegistration();
		try {
			appRegistration.setUri(new URI("docker://fun:party"));
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Mockito.when(this.taskDefinitionRepository.findByTaskName(Mockito.any())).thenReturn(taskDefinition);
		Mockito.when(this.appRegistrationRepository.findAppRegistrationByNameAndTypeAndDefaultVersionIsTrue(Mockito.any(), Mockito.any())).thenReturn(appRegistration);
		List<ConvertScheduleInfo>  convertScheduleInfos = this.k8MigrateSchedulerService.scheduleInfoList();
		assertThat(convertScheduleInfos.size()).isEqualTo(1);
		assertThat(convertScheduleInfos.get(0).getScheduleName()).isEqualTo(DEFAULT_SCHEDULE_NAME);
	}

	@Test
	public void testMigrate() {
		ConvertScheduleInfo scheduleInfo = createFoundationConvertScheduleInfo();
		this.k8MigrateSchedulerService.migrateSchedule(this.scheduler, scheduleInfo);
		final ArgumentCaptor<ScheduleRequest> scheduleRequestArgument = ArgumentCaptor.forClass(ScheduleRequest.class);
		final ArgumentCaptor<String> scheduleNameArg = ArgumentCaptor.forClass(String.class);
		verify(this.scheduler, times(1)).schedule(scheduleRequestArgument.capture());
		verify(this.scheduler, times(1)).unschedule(scheduleNameArg.capture());
		assertThat(scheduleRequestArgument.getValue().getScheduleName()).isEqualTo("defaultScheduleName");
		assertThat(scheduleNameArg.getValue()).isEqualTo(DEFAULT_SCHEDULE_NAME);
	}

	private ConvertScheduleInfo createFoundationConvertScheduleInfo() {
		ConvertScheduleInfo scheduleInfo = new ConvertScheduleInfo();
		scheduleInfo.setScheduleName(DEFAULT_SCHEDULE_NAME);
		scheduleInfo.setTaskDefinitionName(DEFAULT_TASK_DEFINITION_NAME);
		scheduleInfo.setScheduleProperties(new HashMap<>());
		scheduleInfo.setRegisteredAppName(DEFAULT_APP_NAME);
		scheduleInfo.getCommandLineArgs().add(DEFAULT_CMD_ARG);
		scheduleInfo.setTaskResource(Mockito.mock(Resource.class));
		TaskDefinition taskDefinition = TaskDefinition.TaskDefinitionBuilder
				.from(new TaskDefinition("fooTask", "foo"))
				.setTaskName(DEFAULT_TASK_DEFINITION_NAME)
				.setRegisteredAppName(DEFAULT_APP_NAME)
				.build();
		Mockito.when(this.taskDefinitionRepository.findByTaskName(Mockito.any())).thenReturn(taskDefinition);
		scheduleInfo.getAppProperties().put("foo", "bar");
		return this.k8MigrateSchedulerService.enrichScheduleMetadata(scheduleInfo);
	}

	private void validateDBProperties(ConvertScheduleInfo scheduleInfo) {
		assertThat(scheduleInfo.getAppProperties().get("spring.datasource.url")).isEqualTo(DEFAULT_DB_URL);
		assertThat(scheduleInfo.getAppProperties().get("spring.datasource.username")).isEqualTo(DEFAULT_DB_USER_NAME);
		assertThat(scheduleInfo.getAppProperties().get("spring.datasource.password")).isEqualTo(DEFAULT_DB_PASSWORD);
		assertThat(scheduleInfo.getAppProperties().get("spring.datasource.driverClassName")).isEqualTo(DEFAULT_DRIVER_CLASS_NAME);
	}
}
