/*
 * Copyright 2021-2022 the original author or authors.
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

package io.spring.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@EnableConfigurationProperties({ TimestampBatchTaskProperties.class })
public class TimestampBatchTaskConfiguration extends DefaultBatchConfiguration {

	private static final Log logger = LogFactory.getLog(TimestampBatchTaskProperties.class);


	@Autowired
	private TimestampBatchTaskProperties config;

	@Bean
	@ConditionalOnProperty(name = "spring.datasource.driver-class-name", matchIfMissing = true)
	public DataSource dataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
				.addScript("/org/springframework/batch/core/schema-h2.sql")
				.generateUniqueName(true).build();
	}
	/**
	 * Override default transaction isolation level 'ISOLATION_REPEATABLE_READ' which Oracle does not
	 * support.
	 */
	@Bean
	public Step job1step1(JobRepository jobRepository, PlatformTransactionManager springCloudTaskTransactionManager) {
		return new StepBuilder("job1step1", jobRepository)
				.tasklet(getTasklet("Job1 was run with date %s"), springCloudTaskTransactionManager).build();
	}

	private Tasklet getTasklet(String format) {
		return (contribution, chunkContext) -> {
			DateFormat dateFormat = new SimpleDateFormat(config.getFormat());
			logger.info(String.format(format, dateFormat.format(new Date())));
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Job job1(JobRepository jobRepository, Step job1step1) {
		return new JobBuilder("job1", jobRepository)
				.start(job1step1)
				.build();
	}
	@Bean
	public Step job2step1(JobRepository jobRepository,
						  PlatformTransactionManager springCloudTaskTransactionManager
	) {
		return new StepBuilder("job2step1", jobRepository)
				.tasklet(getTasklet("Job2 was run with date %s"), springCloudTaskTransactionManager)
				.build();
	}
	@Bean
	public Job job2(JobRepository jobRepository, Step job2step1) {
		return new JobBuilder("job2", jobRepository)
				.start(job2step1)
				.build();
	}

}

