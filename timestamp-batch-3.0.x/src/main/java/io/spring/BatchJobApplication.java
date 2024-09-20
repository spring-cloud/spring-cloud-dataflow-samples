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

package io.spring;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;

@EnableTask
@SpringBootApplication
public class BatchJobApplication {
    private static final Logger logger = LoggerFactory.getLogger(BatchJobApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(BatchJobApplication.class, args);
    }

    @Bean
    public TimestampTask timestampTask(JobLauncher jobLauncher, Job job1) {
        return new TimestampTask(jobLauncher, job1);
    }

    public static class TimestampTask implements CommandLineRunner {
        private final JobLauncher launcher;
        private final Job job1;


        public TimestampTask(JobLauncher launcher, Job job1) {
            this.launcher = launcher;
            this.job1 = job1;
        }

        @Override
        public void run(String... strings) throws Exception {
            logger.info("starting: {} with {}", job1.getName(), Arrays.asList(strings));
            launcher.run(job1, new JobParameters());
        }
    }
}
