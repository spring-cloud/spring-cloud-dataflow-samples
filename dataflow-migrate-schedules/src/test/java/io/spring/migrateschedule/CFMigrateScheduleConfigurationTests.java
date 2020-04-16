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
import java.util.HashMap;

import io.pivotal.scheduler.SchedulerClient;
import io.spring.migrateschedule.service.AppRegistrationRepository;
import io.spring.migrateschedule.service.CFMigrateSchedulerService;
import io.spring.migrateschedule.service.ConvertScheduleInfo;
import io.spring.migrateschedule.service.MigrateProperties;
import io.spring.migrateschedule.service.TaskDefinitionRepository;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationEnvironments;
import org.cloudfoundry.operations.applications.ApplicationHealthCheck;
import org.cloudfoundry.operations.applications.ApplicationManifest;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.applications.GetApplicationHealthCheckRequest;
import org.cloudfoundry.operations.applications.GetApplicationManifestRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import org.springframework.cloud.dataflow.core.AppRegistration;
import org.springframework.cloud.dataflow.core.TaskDefinition;
import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryConnectionProperties;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleRequest;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CFMigrateScheduleConfigurationTests {

	public static final String DEFAULT_TASK_DEFINITION_NAME = "defaultTaskDefinitionName";
	public static final String DEFAULT_SCHEDULE_NAME = "defaultScheduleName-scdf-" + DEFAULT_TASK_DEFINITION_NAME;
	public static final String DEFAULT_APP_NAME = "defaultAppName";
	public static final String DEFAULT_CMD_ARG = "defaultCmd=MUCHWOW";
	public static final String TASK_LAUNCHER_TASK_NAME_ARG = "--spring.cloud.scheduler.task.launcher.taskName="+ DEFAULT_TASK_DEFINITION_NAME;
	public static final String DEFAULT_BUILD_PACK = "defaultBuildPack";
	public static final String DEFAULT_DATA_FLOW_URI = "http://localhost:9393";

	private CFMigrateSchedulerService cfConvertSchedulerService;
	private CloudFoundryOperations cloudFoundryOperations;
	private CloudFoundryConnectionProperties cloudFoundryConnectionProperties;
	private MigrateProperties migrateProperties;
	private TaskDefinitionRepository taskDefinitionRepository;
	private SchedulerClient schedulerClient;
	private Scheduler scheduler;
	private AppRegistrationRepository appRegistrationRepository;
	private TaskLauncher taskLauncher;

	@BeforeEach
		public void setup() {
		this.cloudFoundryOperations = Mockito.mock(CloudFoundryOperations.class);
		this.schedulerClient = Mockito.mock(SchedulerClient.class);
		this.cloudFoundryConnectionProperties = new CloudFoundryConnectionProperties();
		this.migrateProperties = new MigrateProperties();
		this.taskDefinitionRepository = Mockito.mock(TaskDefinitionRepository.class);
		this.scheduler = Mockito.mock(Scheduler.class);
		this.appRegistrationRepository = Mockito.mock(AppRegistrationRepository.class);
		this.taskLauncher = Mockito.mock(TaskLauncher.class);


		this.cfConvertSchedulerService = new CFMigrateSchedulerService(this.cloudFoundryOperations,
				this.schedulerClient,
				this.cloudFoundryConnectionProperties, this.migrateProperties,
				this.taskDefinitionRepository, new MavenProperties(),
				this.appRegistrationRepository,
				this.taskLauncher) ;

	}

	@Test
	public void testEnrichment() {
		ConvertScheduleInfo scheduleInfo = createFoundationConvertScheduleInfo();
		assertThat(scheduleInfo.getAppProperties().keySet().size()).isEqualTo(5);
		assertThat(scheduleInfo.getAppProperties().get("foo")).isEqualTo("bar");
		assertThat(scheduleInfo.getAppProperties().get("dataflow-server-uri")).isEqualTo(DEFAULT_DATA_FLOW_URI);
		assertThat(scheduleInfo.getAppProperties().get("cloudfoundry.memory")).isEqualTo("1024m");
		assertThat(scheduleInfo.getAppProperties().get("cloudfoundry.health-check")).isEqualTo("port");
		assertThat(scheduleInfo.getAppProperties().get("cloudfoundry.disk")).isEqualTo("1024m");

		assertThat(scheduleInfo.getCommandLineArgs().size()).isEqualTo(2);
		assertThat(scheduleInfo.getCommandLineArgs().get(0)).isEqualTo(DEFAULT_CMD_ARG);
		assertThat(scheduleInfo.getCommandLineArgs().get(1)).isEqualTo("--spring.application.name=defaultTaskDefinitionName");
	}

	@Test
	public void testEnrichmentNoProps() {
		ConvertScheduleInfo scheduleInfo = new ConvertScheduleInfo();
		scheduleInfo.setScheduleName(DEFAULT_SCHEDULE_NAME);
		scheduleInfo.setTaskDefinitionName(DEFAULT_TASK_DEFINITION_NAME);
		scheduleInfo.setScheduleProperties(new HashMap<>());
		scheduleInfo.setRegisteredAppName(DEFAULT_APP_NAME);
		scheduleInfo.getCommandLineArgs().add(TASK_LAUNCHER_TASK_NAME_ARG);
		Mockito.when(cloudFoundryOperations.applications()).thenReturn(new NoPropertyApplication());
		createMockAppRegistration();
		TaskDefinition taskDefinition = TaskDefinition.TaskDefinitionBuilder
				.from(new TaskDefinition("fooTask", "foo"))
				.setTaskName(DEFAULT_TASK_DEFINITION_NAME)
				.setRegisteredAppName(DEFAULT_APP_NAME)
				.setDslText("timestamp")
				.build();
		Mockito.when(this.taskDefinitionRepository.findByTaskName(Mockito.any())).thenReturn(taskDefinition);
		scheduleInfo = this.cfConvertSchedulerService.enrichScheduleMetadata(scheduleInfo);
		assertThat(scheduleInfo.getAppProperties().keySet().size()).isEqualTo(1);
		assertThat(scheduleInfo.getAppProperties().get("dataflow-server-uri")).isEqualTo("http://localhost:9393");
		assertThat(scheduleInfo.getCommandLineArgs().size()).isEqualTo(1);
		assertThat(scheduleInfo.getCommandLineArgs().get(0)).isEqualTo("--spring.application.name=defaultTaskDefinitionName");
	}

	@Test
	public void testNoTaskDefinition() {
		ConvertScheduleInfo scheduleInfo = new ConvertScheduleInfo();
		scheduleInfo.setScheduleName(DEFAULT_SCHEDULE_NAME);
		scheduleInfo.setTaskDefinitionName(DEFAULT_TASK_DEFINITION_NAME);
		scheduleInfo.setScheduleProperties(new HashMap<>());
		scheduleInfo.setRegisteredAppName(DEFAULT_APP_NAME);
		scheduleInfo.getCommandLineArgs().add(DEFAULT_CMD_ARG);
		Mockito.when(cloudFoundryOperations.applications()).thenReturn(new SinglePropertyApplication());
		scheduleInfo = this.cfConvertSchedulerService.enrichScheduleMetadata(scheduleInfo);
		assertThat(scheduleInfo.getTaskResource()).isNull();
	}

	@Test
	public void testMigrate() {
		ConvertScheduleInfo scheduleInfo = createFoundationConvertScheduleInfo();
		this.cfConvertSchedulerService.migrateSchedule(this.scheduler, scheduleInfo);
		final ArgumentCaptor<ScheduleRequest> scheduleRequestArgument = ArgumentCaptor.forClass(ScheduleRequest.class);
		final ArgumentCaptor<String> scheduleNameArg = ArgumentCaptor.forClass(String.class);
		verify(this.scheduler, times(1)).schedule(scheduleRequestArgument.capture());
		verify(this.taskLauncher, times(1)).destroy(scheduleNameArg.capture());
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
		scheduleInfo.getCommandLineArgs().add(TASK_LAUNCHER_TASK_NAME_ARG);

		Mockito.when(cloudFoundryOperations.applications()).thenReturn(new SinglePropertyApplication());
		TaskDefinition taskDefinition = TaskDefinition.TaskDefinitionBuilder
				.from(new TaskDefinition("fooTask", "foo"))
				.setTaskName(DEFAULT_TASK_DEFINITION_NAME)
				.setRegisteredAppName(DEFAULT_APP_NAME)
				.setDslText("timestamp")
				.build();
		Mockito.when(this.taskDefinitionRepository.findByTaskName(Mockito.any())).thenReturn(taskDefinition);
		createMockAppRegistration();
		return this.cfConvertSchedulerService.enrichScheduleMetadata(scheduleInfo);
	}

	public static class SinglePropertyApplication extends AbstractApplications {
		@Override
		public Mono<ApplicationDetail> get(GetApplicationRequest getApplicationRequest) {
			ApplicationDetail applicationDetail = ApplicationDetail.builder()
					.buildpack(DEFAULT_BUILD_PACK)
					.stack("defaultstack")
					.diskQuota(1024)
					.id("defaultappidone")
					.memoryLimit(1024)
					.instances(1)
					.name(DEFAULT_TASK_DEFINITION_NAME)
					.requestedState("requestedState")
					.runningInstances(1)
					.build();
			return Mono.just(applicationDetail);
		}

		@Override
		public Mono<ApplicationEnvironments> getEnvironments(GetApplicationEnvironmentsRequest getApplicationEnvironmentsRequest) {
			return Mono.just(ApplicationEnvironments.builder().userProvided("SPRING_APPLICATION_JSON", "{\"foo\":\"bar\"}").build());
		}
		@Override
		public Mono<ApplicationHealthCheck> getHealthCheck(GetApplicationHealthCheckRequest getApplicationHealthCheckRequest) {
			return Mono.just(ApplicationHealthCheck.PORT);
		}

		@Override
		public Mono<ApplicationManifest> getApplicationManifest(GetApplicationManifestRequest getApplicationManifestRequest) {
			return Mono.just(ApplicationManifest.builder().name(DEFAULT_TASK_DEFINITION_NAME)
					.disk(1024)
					.memory(1024)
					.healthCheckType(ApplicationHealthCheck.PORT)
					.build());
		}

	}
	public static class NoPropertyApplication extends AbstractApplications {

		@Override
		public Mono<ApplicationDetail> get(GetApplicationRequest getApplicationRequest) {
			return Mono.empty();
		}

		@Override
		public Mono<ApplicationEnvironments> getEnvironments(GetApplicationEnvironmentsRequest getApplicationEnvironmentsRequest) {
			return Mono.empty();
		}

		@Override
		public Mono<ApplicationHealthCheck> getHealthCheck(GetApplicationHealthCheckRequest getApplicationHealthCheckRequest) {
			return Mono.empty();
		}

		@Override
		public Mono<ApplicationManifest> getApplicationManifest(GetApplicationManifestRequest getApplicationManifestRequest) {
			return Mono.empty();
		}
	}

	private void createMockAppRegistration() {
		AppRegistration appRegistration = new AppRegistration();
		try {
			appRegistration.setUri(new URI("docker://fun:party"));
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		Mockito.when(this.appRegistrationRepository.findAppRegistrationByNameAndTypeAndDefaultVersionIsTrue(Mockito.any(), Mockito.any())).thenReturn(appRegistration);
	}
}
