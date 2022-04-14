/*
 * Copyright 2020-2022 the original author or authors.
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

package io.spring.scenariotask.configuration;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Configure the Task and or Batch components of the test application.
 *
 * @author Glenn Renfro
 */
@EnableTask
@EnableBatchProcessing
@Configuration
@EnableConfigurationProperties(ScenarioProperties.class)
public class ScenarioTaskConfiguration {

	private static final Log logger = LogFactory.getLog(ScenarioTaskConfiguration.class);

	@Configuration
	@ConditionalOnProperty(
			value = "io.spring.launchBatchJob",
			havingValue = "true",
			matchIfMissing = true)
	static class BatchConfig {
		@Autowired
		public JobBuilderFactory jobBuilderFactory;

		@Autowired
		public StepBuilderFactory stepBuilderFactory;

		@Autowired
		public JobExplorer jobExplorer;

		@Autowired
		public ScenarioProperties properties;

		@Bean
		public Job pausedemoAgain() {
			SimpleJobBuilder jobBuilder = this.jobBuilderFactory.get(properties.getJobName())
					.start(this.stepBuilderFactory.get(properties.getStepName())
							.tasklet((contribution, chunkContext) -> {
								logger.info(String.format("%s is starting", properties.getStepName()));
								if (properties.getPauseInSeconds() > 0) {
									logger.info(String.format("%s is pausing", properties.getStepName()));
									Thread.sleep(properties.getPauseInSeconds() * 1000);
								}
								logger.info(String.format("%s is completing", properties.getStepName()));

								if (jobExecutionCount() == 1 && properties.isFailBatch()) {
									throw new ExpectedException("Exception thrown during Batch Execution");
								}
								return RepeatStatus.FINISHED;
							})
							.build());
			if (this.properties.isIncludeRunidIncrementer()) {
				jobBuilder.incrementer(new RunIdIncrementer());
			}
			return jobBuilder.build();
		}

		/**
		 * Override default transaction isolation level 'ISOLATION_REPEATABLE_READ' which Oracle does not
		 * support.
		 */
		@Configuration
		@ConditionalOnProperty(value = "spring.datasource.driver", havingValue = "oracle.jdbc.OracleDriver")
		static class OracleBatchConfig {
			@Bean
			BatchConfigurer oracleBatchConfigurer(DataSource dataSource) {
				return new DefaultBatchConfigurer() {
					@Override
					public JobRepository getJobRepository() {
						JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
						factoryBean.setDatabaseType("ORACLE");
						factoryBean.setDataSource(dataSource);
						factoryBean.setTransactionManager(getTransactionManager());
						factoryBean.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
						try {
							return factoryBean.getObject();
						}
						catch (Exception e) {
							throw new BeanCreationException(e.getMessage(), e);
						}
					}

					@Override
					public DataSourceTransactionManager getTransactionManager() {
						return new DataSourceTransactionManager(dataSource);
					}
				};
			}
		}

		private int jobExecutionCount() {
			JobInstance jobInstance = jobExplorer.getLastJobInstance(this.properties.getJobName());
			List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
			return jobExecutions.size();
		}
	}

	/**
	 * Displays simple log message.   If user specifies {@code io.spring.fail-task=true} a {@link ExpectedException} is thrown.
	 *
	 * @return ApplicationRunner instance for the app.
	 */
	@Bean
	public ApplicationRunner applicationRunner(ScenarioProperties properties) {
		return args -> {
			logger.info("ApplicationRunner Executing for ScenarioTaskApplication");
			if (properties.isFailTask()) {
				throw new ExpectedException("Exception thrown during Task Execution");
			}
		};
	}
}
