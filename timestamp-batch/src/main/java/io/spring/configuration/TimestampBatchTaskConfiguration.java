/*
 * Copyright 2021 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ TimestampBatchTaskProperties.class })
public class TimestampBatchTaskConfiguration {

	private static final Log logger = LogFactory.getLog(TimestampBatchTaskProperties.class);

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	private TimestampBatchTaskProperties config;

	@Bean
	public Job job1() {
		return jobBuilderFactory.get("job1")
				.start(stepBuilderFactory.get("job1step1")
						.tasklet(new Tasklet() {
							@Override
							public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
									throws Exception {
								DateFormat dateFormat = new SimpleDateFormat(config.getFormat());
								logger.info(String.format("Job1 was run with date %s", dateFormat.format(new Date())));
								return RepeatStatus.FINISHED;
							}
						})
						.build())
				.build();
	}

	@Bean
	public Job job2() {
		return jobBuilderFactory.get("job2")
				.start(stepBuilderFactory.get("job2step1")
						.tasklet(new Tasklet() {
							@Override
							public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
									throws Exception {
								DateFormat dateFormat = new SimpleDateFormat(config.getFormat());
								logger.info(String.format("Job2 was run with date %s", dateFormat.format(new Date())));
								return RepeatStatus.FINISHED;
							}
						})
						.build())
				.build();
	}

}

