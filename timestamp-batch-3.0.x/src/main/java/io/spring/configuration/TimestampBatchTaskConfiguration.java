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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties({ TimestampBatchTaskProperties.class })
public class TimestampBatchTaskConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(TimestampBatchTaskProperties.class);

	@Autowired
	private TimestampBatchTaskProperties config;

	@Bean(name = "job1step1")
	public Step job1step1(JobRepository jobRepository, PlatformTransactionManager springCloudTaskTransactionManager) {
		return new StepBuilder("job1step1", jobRepository)
				.tasklet(getTasklet("Job1 was run with format %s and result=date %s"), springCloudTaskTransactionManager).build();
	}

	private Tasklet getTasklet(String format) {
		return (contribution, chunkContext) -> {
			DateFormat dateFormat = new SimpleDateFormat(config.getFormat());
			contribution.getStepExecution().getExecutionContext().put("ctx1", "exec1");
			contribution.getStepExecution().getJobExecution().getExecutionContext().put("job-ctx1", "exec-job1");
			logger.info("{}:{}", contribution.getStepExecution().getStepName(), contribution.getStepExecution().getExecutionContext());
			logger.info("{}:{}", contribution.getStepExecution().getJobExecution().getJobInstance().getJobName(), contribution.getStepExecution().getJobExecution().getExecutionContext());
			logger.info(String.format(format, format, dateFormat.format(new Date())));
			return RepeatStatus.FINISHED;
		};
	}

	@Bean(name = "job1")
	public Job job1(JobRepository jobRepository, @Qualifier("job1step1") Step job1step1) {
		return new JobBuilder("job1", jobRepository)
				.start(job1step1)
				.build();
	}

}

