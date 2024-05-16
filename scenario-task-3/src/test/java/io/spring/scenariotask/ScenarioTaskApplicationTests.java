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

package io.spring.scenariotask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.scenariotask.configuration.ExpectedException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.task.listener.TaskException;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.cloud.task.repository.TaskExplorer;
import org.springframework.cloud.task.repository.support.SimpleTaskExplorer;
import org.springframework.cloud.task.repository.support.TaskExecutionDaoFactoryBean;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
public class ScenarioTaskApplicationTests {

	private static DataSource dataSource;

	private static JobExplorer jobExplorer;

	private static TaskExplorer taskExplorer;

	@Container
	private static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
			.withDatabaseName("integration-tests-db")
			.withUsername("sa")
			.withPassword("sa");

	@BeforeAll
	public static void initializeDB() throws Exception {
		DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
		driverManagerDataSource.setDriverClassName("org.postgresql.Driver");
		driverManagerDataSource.setUrl(postgreSQLContainer.getJdbcUrl());
		driverManagerDataSource.setUsername(postgreSQLContainer.getUsername());
		driverManagerDataSource.setPassword(postgreSQLContainer.getPassword());
		dataSource = driverManagerDataSource;
		jobExplorer = jobExplorer();
		taskExplorer = taskExplorer();
	}

	private static JobExplorer jobExplorer() throws Exception {
		JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setSerializer(new DefaultExecutionContextSerializer());
		factoryBean.setJdbcOperations(new JdbcTemplate(dataSource));
		factoryBean.setJobKeyGenerator(new DefaultJobKeyGenerator());
		factoryBean.setConversionService(new DefaultConversionService());
		return factoryBean.getObject();
	}

	private static TaskExplorer taskExplorer() {
		TaskExecutionDaoFactoryBean taskExecutionDaoFactoryBean = new TaskExecutionDaoFactoryBean(dataSource);
		return new SimpleTaskExplorer(taskExecutionDaoFactoryBean);
	}

	@Test
	void testSuccessfulTask() {
		String taskName = "taskSuccessfulTask";
		List<String> args = getTaskLaunchArgs(taskName);
		SpringApplication.run(ScenarioTaskApplication.class,
				args.toArray(new String[0]));
		List<TaskExecution> taskExecutions = getTaskExecutions(taskName);
		assertThat(taskExecutions.size()).isEqualTo(1);
		assertThat(taskExecutions.get(0).getExitCode()).isEqualTo(0);
	}

	@Test
	void testFailTask() {
		String taskName = "testFailTask";
		List<String> args = getTaskLaunchArgs(taskName);
		args.add("--io.spring.fail-task=true");
		launchAppVerifyTaskFails(args, IllegalStateException.class);
		List<TaskExecution> taskExecutions = getTaskExecutions(taskName);
		assertThat(taskExecutions.size()).isEqualTo(1);
		assertThat(taskExecutions.get(0).getExitCode()).isEqualTo(1);
	}

	@Test
	void testSuccessTaskWithFailedBatchAndRestart() {
		final String jobName = "testSuccessTaskWithFailedBatchAndRestart";
		SpringApplication.run(ScenarioTaskApplication.class,
				getFailBatchArgs(jobName).toArray(new String[0])
		);
		List<JobExecution> jobExecutions = getJobExecutionsForLastJobInstance(jobName);
		assertThat(jobExecutions.size()).isEqualTo(1);
		assertThat(jobExecutions.get(0).getExitStatus().getExitCode()).isEqualTo(ExitStatus.FAILED.getExitCode());
		assertThat(jobExecutions.get(0).getExitStatus().getExitDescription()).startsWith("io.spring.scenariotask.configuration.ExpectedException");

		SpringApplication.run(ScenarioTaskApplication.class,
				getFailBatchArgs(jobName).toArray(new String[0]));
		jobExecutions = getJobExecutionsForLastJobInstance(jobName);
		assertThat(jobExecutions.size()).isEqualTo(2);
		assertThat(jobExecutions.get(0).getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
	}

	@Test
	void testSuccessTaskSuccessBatchAndRestartFailure() throws Exception{
		final String jobName = "testSuccessTaskSuccessBatchAndRestartFailure";
		List<String> args = getSuccessBatchArgs(jobName);
		args.add("--io.spring.include-runid-incrementer=true");
		SpringApplication.run(ScenarioTaskApplication.class, args.toArray(new String[0]));
		List<JobExecution> jobExecutions = getJobExecutionsForLastJobInstance(jobName);
		assertThat(jobExecutions.size()).isEqualTo(1);
		assertThat(jobExecutions.get(0).getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());

		SpringApplication.run(ScenarioTaskApplication.class,
				args.toArray(new String[0]));
		assertThat(jobExplorer.getJobInstanceCount(jobName)).isEqualTo(2);
	}


	@Test
	void testFailTaskWithFailedBatchAndRestart() {
		final String jobName = "testFailTaskWithFailedBatchAndRestart";
		final String taskName = "testFailTaskWithFailedBatchAndRestartTask";
		List<String> args = getFailBatchArgs(jobName);
		args.add("--spring.application.name=" + taskName);
		args.add("--spring.cloud.task.batch.fail-on-job-failure=true");
		launchAppVerifyTaskFails(args, TaskException.class);
		List<JobExecution> jobExecutions = getJobExecutionsForLastJobInstance(jobName);
		assertThat(jobExecutions.size()).isEqualTo(1);
		assertThat(jobExecutions.get(0).getExitStatus().getExitCode()).isEqualTo(ExitStatus.FAILED.getExitCode());
		assertThat(jobExecutions.get(0).getExitStatus().getExitDescription()).startsWith("io.spring.scenariotask.configuration.ExpectedException");
		List<TaskExecution> taskExecutions = getTaskExecutions(taskName);
		assertThat(taskExecutions.size()).isEqualTo(1);
		assertThat(taskExecutions.get(0).getExitCode()).isEqualTo(1);

		SpringApplication.run(ScenarioTaskApplication.class,
				args.toArray(new String[0]));
		jobExecutions = getJobExecutionsForLastJobInstance(jobName);
		assertThat(jobExecutions.size()).isEqualTo(2);
		assertThat(jobExecutions.get(0).getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
	}

	@Test
	void testSuccessfulTaskBatch() {
		final String jobName = "testSuccessfulTaskBatch";
		SpringApplication.run(ScenarioTaskApplication.class,
				getSuccessBatchArgs(jobName).toArray(new String[0]));
		List<JobExecution> jobExecutions = getJobExecutionsForLastJobInstance(jobName);
		assertThat(jobExecutions.size()).isEqualTo(1);
		assertThat(jobExecutions.get(0).getExitStatus().getExitCode()).isEqualTo(ExitStatus.COMPLETED.getExitCode());
	}

	private void launchAppVerifyTaskFails(List<String> args, Class clazz) {
		assertThatThrownBy(() -> {
			SpringApplication.run(ScenarioTaskApplication.class, args.toArray(new String[0]));
		}).isInstanceOf(clazz);
	}

	private List<JobExecution> getJobExecutionsForLastJobInstance(String jobName) {
		long instanceId = jobExplorer.getLastJobInstance(jobName).getInstanceId();
		return jobExplorer.getJobExecutions(jobExplorer.getJobInstance(instanceId));
	}

	private List<String> getTaskLaunchArgs(String taskName) {
		List<String> args = new ArrayList<>(getDatabaseArgs());
		args.add("--io.spring.launchBatchJob=false");
		args.add("--spring.application.name=" + taskName);
		return args;
	}

	private List<String> getDatabaseArgs() {
		List<String> args = Arrays.asList(
				"--spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
				"--spring.datasource.driverClassName=" + "org.postgresql.Driver",
				"--spring.datasource.username=" + postgreSQLContainer.getUsername(),
				"--spring.datasource.password=" + postgreSQLContainer.getPassword());
		return args;
	}

	private List<String> getSuccessBatchArgs(String jobName) {
		List<String> args = new ArrayList<>(getDatabaseArgs());
		args.add("--io.spring.job-name=" + jobName);
		args.add("--spring.batch.jdbc.initialize-schema=always");
		return args;
	}

	private List<String> getFailBatchArgs(String jobName) {
		List<String> args = new ArrayList<>(getSuccessBatchArgs(jobName));
		args.add("--io.spring.fail-batch=true");
		return args;
	}

	private List<TaskExecution> getTaskExecutions(String taskName) {
		Page<TaskExecution> taskExecutionPage = taskExplorer.findTaskExecutionsByName(taskName, PageRequest.of(0, 5));
		return taskExecutionPage.getContent();
	}
}
