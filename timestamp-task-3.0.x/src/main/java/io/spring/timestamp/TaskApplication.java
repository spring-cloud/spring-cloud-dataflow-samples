/*
 * Copyright 2021  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.timestamp;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot Application that has tasks enabled.
 */
@EnableTask
@SpringBootApplication
@EnableConfigurationProperties({TimestampTaskProperties.class})
public class TaskApplication {

	private static final Logger logger = LoggerFactory.getLogger(TaskApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TaskApplication.class, args);
	}

	@Bean
	public TimestampTask timeStampTask() {
		return new TimestampTask();
	}

	/**
	 * A commandline runner that prints a timestamp.
	 */
	public static class TimestampTask implements CommandLineRunner {

		@Autowired
		private TimestampTaskProperties config;

		@Override
		public void run(String... arguments) throws Exception {
			List<String> args = Arrays.asList(arguments);
			logger.info("starting with {}", args);
			DateFormat dateFormat = new SimpleDateFormat(this.config.getFormat());
			logger.info("completed at:{} with args {}", dateFormat.format(new Date()), args);
		}
	}
}
