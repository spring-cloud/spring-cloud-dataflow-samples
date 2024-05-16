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
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configure the Task and or Batch components of the test application.
 *
 * @author Glenn Renfro
 */
@Configuration
@EnableConfigurationProperties(ScenarioProperties.class)
public class ScenarioTaskConfiguration {

    private static final Log logger = LogFactory.getLog(ScenarioTaskConfiguration.class);


    @Configuration
    @ConditionalOnProperty(value = "io.spring.launch-batch-job", havingValue = "true", matchIfMissing = true)
    static class BatchConfig {
        private int jobExecutionCount(ScenarioProperties properties, JobExplorer jobExplorer) {
            JobInstance jobInstance = jobExplorer.getLastJobInstance(properties.getJobName());
            List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
            return jobExecutions.size();
        }

        Tasklet getTaskLet(
                ScenarioProperties properties,
                JobExplorer jobExplorer
        ) {
            return (contribution, chunkContext) -> {
                logger.info(String.format("%s is starting", properties.getStepName()));
                if (properties.getPauseInSeconds() > 0) {
                    logger.info(String.format("%s is pausing for %d seconds", properties.getStepName(), properties.getPauseInSeconds()));
                    Thread.sleep(properties.getPauseInSeconds() * 1000);
                }
                logger.info(String.format("%s is completing", properties.getStepName()));
                if (jobExecutionCount(properties, jobExplorer) == 1 && properties.isFailBatch()) {
                    throw new ExpectedException("Exception thrown during Batch Execution");
                }
                return RepeatStatus.FINISHED;
            };
        }

        @Bean
        public Step jobStep(
                ScenarioProperties properties,
                JobRepository jobRepository,
                JobExplorer jobExplorer,
                @Qualifier("springCloudTaskTransactionManager") PlatformTransactionManager platformTransactionManager
        ) {
            return new StepBuilder(properties.getStepName(), jobRepository)
                    .allowStartIfComplete(true)
                    .tasklet(getTaskLet(properties, jobExplorer), platformTransactionManager)
                    .build();
        }

        @Bean
        public Job job(ScenarioProperties properties, JobRepository jobRepository, Step jobStep) {
            logger.info("properties=:" + properties);
            SimpleJobBuilder jobBuilder = new JobBuilder(properties.getJobName(), jobRepository).start(jobStep);
            if (properties.isIncludeRunidIncrementer()) {
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
        @EnableBatchProcessing
        static class OracleBatchConfig {
            @Bean
            JobRepository jobRepositoryFactoryBean(DataSource dataSource) {
                JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
                factoryBean.setDatabaseType("ORACLE");
                factoryBean.setDataSource(dataSource);
                factoryBean.setTransactionManager(platformTransactionManager(dataSource));
                factoryBean.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
                try {
                    return factoryBean.getObject();
                } catch (Exception e) {
                    throw new BeanCreationException(e.getMessage(), e);
                }
            }

            @Bean
            PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
                return new DataSourceTransactionManager(dataSource);
            }

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
            if (!properties.isLaunchBatchJob() && properties.getPauseInSeconds() > 0) {
                logger.info(String.format("Task is pausing for %d seconds", properties.getPauseInSeconds()));
                Thread.sleep(properties.getPauseInSeconds() * 1000);
            }
            if (properties.isFailTask()) {
                throw new ExpectedException("Exception thrown during Task Execution");
            }
        };
    }
}
