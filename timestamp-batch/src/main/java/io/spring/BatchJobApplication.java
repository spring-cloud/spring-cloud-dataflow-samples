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

    public static void main(String[] args) {
        SpringApplication.run(BatchJobApplication.class, args);
    }

    @Bean
    public TimestampTask timestampTask(JobLauncher jobLauncher, Job job1, Job job2) {
        return new TimestampTask(jobLauncher, job1, job2);
    }

    public static class TimestampTask implements CommandLineRunner {
        private final JobLauncher launcher;
        private final Job job1;
        private final Job job2;

        public TimestampTask(JobLauncher launcher, Job job1, Job job2) {
            this.launcher = launcher;
            this.job1 = job1;
            this.job2 = job2;
        }

        @Override
        public void run(String... strings) throws Exception {
            launcher.run(job1, new JobParameters());
            launcher.run(job2, new JobParameters());
        }
    }
}
