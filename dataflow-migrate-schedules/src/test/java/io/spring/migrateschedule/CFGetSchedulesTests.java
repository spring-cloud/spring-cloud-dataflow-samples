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

package io.spring.migrateschedule;


import java.util.ArrayList;
import java.util.List;

import io.pivotal.scheduler.SchedulerClient;
import io.pivotal.scheduler.v1.jobs.CreateJobRequest;
import io.pivotal.scheduler.v1.jobs.CreateJobResponse;
import io.pivotal.scheduler.v1.jobs.DeleteJobRequest;
import io.pivotal.scheduler.v1.jobs.DeleteJobScheduleRequest;
import io.pivotal.scheduler.v1.jobs.ExecuteJobRequest;
import io.pivotal.scheduler.v1.jobs.ExecuteJobResponse;
import io.pivotal.scheduler.v1.jobs.GetJobRequest;
import io.pivotal.scheduler.v1.jobs.GetJobResponse;
import io.pivotal.scheduler.v1.jobs.Job;
import io.pivotal.scheduler.v1.jobs.JobSchedule;
import io.pivotal.scheduler.v1.jobs.Jobs;
import io.pivotal.scheduler.v1.jobs.ListJobHistoriesRequest;
import io.pivotal.scheduler.v1.jobs.ListJobHistoriesResponse;
import io.pivotal.scheduler.v1.jobs.ListJobScheduleHistoriesRequest;
import io.pivotal.scheduler.v1.jobs.ListJobScheduleHistoriesResponse;
import io.pivotal.scheduler.v1.jobs.ListJobSchedulesRequest;
import io.pivotal.scheduler.v1.jobs.ListJobSchedulesResponse;
import io.pivotal.scheduler.v1.jobs.ListJobsRequest;
import io.pivotal.scheduler.v1.jobs.ListJobsResponse;
import io.pivotal.scheduler.v1.jobs.ScheduleJobRequest;
import io.pivotal.scheduler.v1.jobs.ScheduleJobResponse;
import io.spring.migrateschedule.service.CFMigrateSchedulerService;
import io.spring.migrateschedule.service.ConvertScheduleInfo;
import io.spring.migrateschedule.service.MigrateProperties;
import io.spring.migrateschedule.service.TaskDefinitionRepository;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationEnvironments;
import org.cloudfoundry.operations.applications.ApplicationHealthCheck;
import org.cloudfoundry.operations.applications.ApplicationManifest;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.applications.GetApplicationHealthCheckRequest;
import org.cloudfoundry.operations.applications.GetApplicationManifestRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.spaces.AllowSpaceSshRequest;
import org.cloudfoundry.operations.spaces.CreateSpaceRequest;
import org.cloudfoundry.operations.spaces.DeleteSpaceRequest;
import org.cloudfoundry.operations.spaces.DisallowSpaceSshRequest;
import org.cloudfoundry.operations.spaces.GetSpaceRequest;
import org.cloudfoundry.operations.spaces.RenameSpaceRequest;
import org.cloudfoundry.operations.spaces.SpaceDetail;
import org.cloudfoundry.operations.spaces.SpaceSshAllowedRequest;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.operations.spaces.Spaces;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryConnectionProperties;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;

import static org.assertj.core.api.Assertions.assertThat;

public class CFGetSchedulesTests {

	private static final String DEFAULT_SPACE = "TESTSPACE";
	private static final String DEFAULT_APPLICATION_ID = "TEST_APPLICATION_ID";
	private static final String DEFAULT_COMMAND_ARG_PROP = "foo=bar";
	private static final String DEFAULT_SCHEDULE_EXPRESSION = "*/1 * ? * *";
	private static final String DEFAULT_COMMAND_ARG = CFMigrateSchedulerService.JAR_LAUNCHER + " " + DEFAULT_COMMAND_ARG_PROP;

	private CFMigrateSchedulerService cfConvertSchedulerService;
	private CloudFoundryOperations cloudFoundryOperations;
	private CloudFoundryConnectionProperties cloudFoundryConnectionProperties;
	private MigrateProperties migrateProperties;
	private TaskDefinitionRepository taskDefinitionRepository;
	private SchedulerClient schedulerClient;
	private Scheduler scheduler;

	@BeforeEach
		public void setup() {
		this.cloudFoundryOperations = Mockito.mock(CloudFoundryOperations.class);
		this.schedulerClient = Mockito.mock(SchedulerClient.class);
		this.cloudFoundryConnectionProperties = new CloudFoundryConnectionProperties();
		this.cloudFoundryConnectionProperties.setSpace(DEFAULT_SPACE);
		this.migrateProperties = new MigrateProperties();
		this.taskDefinitionRepository = Mockito.mock(TaskDefinitionRepository.class);
		this.scheduler = Mockito.mock(Scheduler.class);
		this.cfConvertSchedulerService = new CFMigrateSchedulerService(this.cloudFoundryOperations,
				this.schedulerClient,
				this.cloudFoundryConnectionProperties, this.migrateProperties,
				this.taskDefinitionRepository, new MavenProperties()) ;
	}


	@Test
	public void testGetSchedules() {
		Mockito.when(this.cloudFoundryOperations.applications()).thenReturn(new TestApplications());
		Mockito.when(this.cloudFoundryOperations.spaces()).thenReturn(new TestSpaces());
		Mockito.when(this.schedulerClient.jobs()).thenReturn(new TestJobs());
		List<ConvertScheduleInfo> convertScheduleInfos = this.cfConvertSchedulerService.getSchedules(1);
		assertThat(convertScheduleInfos.size()).isEqualTo(2);

		ConvertScheduleInfo convertScheduleInfo = convertScheduleInfos.get(0);
		baseTests(convertScheduleInfo,"JOB1", DEFAULT_APPLICATION_ID);
		assertThat(convertScheduleInfo.getCommandLineArgs().size()).isEqualTo(1);
		assertThat(convertScheduleInfo.getCommandLineArgs().get(0)).isEqualTo(DEFAULT_COMMAND_ARG_PROP);

		convertScheduleInfo = convertScheduleInfos.get(1);
		assertThat(convertScheduleInfo.getCommandLineArgs().size()).isEqualTo(0);
		baseTests(convertScheduleInfo,"JOB2", DEFAULT_APPLICATION_ID);
	}

	private void baseTests(ConvertScheduleInfo convertScheduleInfo, String scheduleName, String taskDefinitionName) {
		assertThat(convertScheduleInfo.getScheduleProperties().get("spring.cloud.scheduler.cron.expression")).isEqualTo("*/1 * ? * *");
		assertThat(convertScheduleInfo.getScheduleName()).isEqualTo(scheduleName);
		assertThat(convertScheduleInfo.getTaskDefinitionName()).isEqualTo(taskDefinitionName);
	}

	private static class TestApplications extends AbstractApplications {
		@Override
		public Mono<ApplicationDetail> get(GetApplicationRequest request) {
			return null;
		}

		@Override
		public Mono<ApplicationManifest> getApplicationManifest(GetApplicationManifestRequest request) {
			return null;
		}

		@Override
		public Mono<ApplicationEnvironments> getEnvironments(GetApplicationEnvironmentsRequest request) {
			return null;
		}

		@Override
		public Mono<ApplicationHealthCheck> getHealthCheck(GetApplicationHealthCheckRequest request) {
			return null;
		}

		@Override
		public Flux<ApplicationSummary> list() {
			ApplicationSummary applicationSummary = ApplicationSummary.builder().
					id(DEFAULT_APPLICATION_ID).
					diskQuota(1024).
					instances(1).
					memoryLimit(1024).
					name(DEFAULT_APPLICATION_ID).
					requestedState("GOOD").
					runningInstances(1).
					build();
			Flux<ApplicationSummary> applicationSummaries = Flux.just(applicationSummary);
			return applicationSummaries;
		}
	}

	private static class TestSpaces implements Spaces {

		@Override
		public Mono<Void> allowSsh(AllowSpaceSshRequest request) {
			return null;
		}

		@Override
		public Mono<Void> create(CreateSpaceRequest request) {
			return null;
		}

		@Override
		public Mono<Void> delete(DeleteSpaceRequest request) {
			return null;
		}

		@Override
		public Mono<Void> disallowSsh(DisallowSpaceSshRequest request) {
			return null;
		}

		@Override
		public Mono<SpaceDetail> get(GetSpaceRequest request) {
			return null;
		}

		@Override
		public Flux<SpaceSummary> list() {
			SpaceSummary spaceSummary = SpaceSummary.builder().id(DEFAULT_SPACE).name(DEFAULT_SPACE).build();
			Flux<SpaceSummary> spaceSummaries = Flux.just(spaceSummary);
			return spaceSummaries;
		}

		@Override
		public Mono<Void> rename(RenameSpaceRequest request) {
			return null;
		}

		@Override
		public Mono<Boolean> sshAllowed(SpaceSshAllowedRequest request) {
			return null;
		}
	}

	public static class TestJobs implements Jobs {

		@Override
		public Mono<CreateJobResponse> create(CreateJobRequest createJobRequest) {
			return null;
		}

		@Override
		public Mono<Void> delete(DeleteJobRequest deleteJobRequest) {
			return null;
		}

		@Override
		public Mono<Void> deleteSchedule(DeleteJobScheduleRequest deleteJobScheduleRequest) {
			return null;
		}

		@Override
		public Mono<ExecuteJobResponse> execute(ExecuteJobRequest executeJobRequest) {
			return null;
		}

		@Override
		public Mono<GetJobResponse> get(GetJobRequest getJobRequest) {
			return null;
		}

		@Override
		public Mono<ListJobsResponse> list(ListJobsRequest listJobsRequest) {
			Job job = Job.builder().id("JOB1").name("JOB1").
					command(DEFAULT_COMMAND_ARG).
					applicationId(DEFAULT_APPLICATION_ID).
					jobSchedule(JobSchedule.builder().
							expression("*/1 * ? * *").
							build()).
					build();
			List<Job> jobList = new ArrayList<>();
			jobList.add(job);

			job = Job.builder().id("JOB2").name("JOB2").
					applicationId(DEFAULT_APPLICATION_ID).
					jobSchedule(JobSchedule.builder().
							expression(DEFAULT_SCHEDULE_EXPRESSION).
							build()).
					build();
			jobList.add(job);
			ListJobsResponse listJobsResponse = ListJobsResponse.builder().addAllResources(jobList).build();
			return Mono.just(listJobsResponse);
		}

		@Override
		public Mono<ListJobHistoriesResponse> listHistories(ListJobHistoriesRequest listJobHistoriesRequest) {
			return null;
		}

		@Override
		public Mono<ListJobScheduleHistoriesResponse> listScheduleHistories(ListJobScheduleHistoriesRequest listJobScheduleHistoriesRequest) {
			return null;
		}

		@Override
		public Mono<ListJobSchedulesResponse> listSchedules(ListJobSchedulesRequest listJobSchedulesRequest) {
			return null;
		}

		@Override
		public Mono<ScheduleJobResponse> schedule(ScheduleJobRequest scheduleJobRequest) {
			return null;
		}
	}

}
