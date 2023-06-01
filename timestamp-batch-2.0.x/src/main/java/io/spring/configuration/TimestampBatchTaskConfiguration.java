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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

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

	@Bean
	public Job job1() {
		return jobBuilderFactory.get("job1")
				.start(stepBuilderFactory.get("job1step1")
						.tasklet((contribution, chunkContext) -> {
							DateFormat dateFormat = new SimpleDateFormat(config.getFormat());
							logger.info(String.format("Job1 was run with date %s", dateFormat.format(new Date())));
							return RepeatStatus.FINISHED;
						})
						.build())
				.build();
	}

	@Bean
	public Job job2() {
		return jobBuilderFactory.get("job2")
				.start(stepBuilderFactory.get("job2step1")
						.tasklet((contribution, chunkContext) -> {
							DateFormat dateFormat = new SimpleDateFormat(config.getFormat());
							logger.info(String.format("Job2 was run with date %s", dateFormat.format(new Date())));
							return RepeatStatus.FINISHED;
						})
						.build())
				.build();
	}

}

