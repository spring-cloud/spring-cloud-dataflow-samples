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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.spring.migrateschedule.configuration.BatchConfiguration;
import io.spring.migrateschedule.service.AppRegistrationRepository;
import io.spring.migrateschedule.service.ConvertScheduleInfo;
import io.spring.migrateschedule.service.MigrateScheduleService;
import io.spring.migrateschedule.service.TaskDefinitionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.deployer.spi.cloudfoundry.CloudFoundryDeployerAutoConfiguration;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest
@EnableAutoConfiguration(exclude = {CloudFoundryDeployerAutoConfiguration.class})
@ContextConfiguration(classes = { BatchIntegrationTests.BatchTestConfiguration.class, BatchConfiguration.class})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BatchIntegrationTests {

	public static final String DEFAULT_SCHEDULE_NAME = "defaultScheduleName-scdf-myapp";
	public static final String DEFAULT_TASK_DEFINITION_NAME = "defaultTaskDefinitionName";
	public static final String DEFAULT_APP_NAME = "defaultAppName";

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private MigrateScheduleService migrateScheduleService;

	@Autowired
	private JobExplorer jobExplorer;

	@MockBean
	private TaskDefinitionRepository taskDefinitionRepository;

	@MockBean
	private AppRegistrationRepository appRegistrationRepository;

	@MockBean
	private Scheduler scheduler;

	@Test
	public void baseTest() throws Exception{
		final ArgumentCaptor<Scheduler> schedulerArgumentCaptor = ArgumentCaptor.forClass(Scheduler.class);
		final ArgumentCaptor<ConvertScheduleInfo> scheduleInfoArgumentCaptor = ArgumentCaptor.forClass(ConvertScheduleInfo.class);
		verify(this.migrateScheduleService, times(2)).enrichScheduleMetadata(scheduleInfoArgumentCaptor.capture());
		verify(this.migrateScheduleService, times(2)).migrateSchedule(schedulerArgumentCaptor.capture(), scheduleInfoArgumentCaptor.capture());
		assertThat(this.jobExplorer.getJobInstanceCount("migrationJob")).isEqualTo(1);
		JobInstance jobInstance = this.jobExplorer.getJobInstances("migrationJob",0, 1).get(0);
		List<JobExecution> jobExecutions = this.jobExplorer.getJobExecutions(jobInstance);
		assertThat(jobExecutions.size()).isEqualTo(1);
		assertThat(jobExecutions.get(0).getExitStatus().getExitCode()).isEqualTo("COMPLETED");
	}

	@Configuration
	public static class BatchTestConfiguration {
		@Bean
		public MigrateScheduleService convertScheduleService() {
			MigrateScheduleService migrateScheduleService = mock(MigrateScheduleService.class);
			ConvertScheduleInfo scheduleInfo = new ConvertScheduleInfo();
			scheduleInfo.setScheduleName(DEFAULT_SCHEDULE_NAME);
			scheduleInfo.setTaskDefinitionName(DEFAULT_TASK_DEFINITION_NAME);
			scheduleInfo.setScheduleProperties(new HashMap<>());
			scheduleInfo.setRegisteredAppName(DEFAULT_APP_NAME);
			List<ConvertScheduleInfo> schedules = new ArrayList<>();
			schedules.add(scheduleInfo);
			scheduleInfo = new ConvertScheduleInfo();
			scheduleInfo.setScheduleName(DEFAULT_SCHEDULE_NAME + 1);
			scheduleInfo.setTaskDefinitionName(DEFAULT_TASK_DEFINITION_NAME + 1);
			scheduleInfo.setScheduleProperties(new HashMap<>());
			scheduleInfo.setRegisteredAppName(DEFAULT_APP_NAME + 1);
			schedules.add(scheduleInfo);
			when(migrateScheduleService.scheduleInfoList()).thenReturn(schedules);
			when(migrateScheduleService.enrichScheduleMetadata(any())).thenReturn(scheduleInfo);
			return migrateScheduleService;
		}
	}
}
