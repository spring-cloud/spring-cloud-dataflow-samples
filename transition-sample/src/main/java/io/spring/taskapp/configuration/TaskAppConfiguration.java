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

package io.spring.taskapp.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Establishes the configuration for this application.
 *
 * @author Glenn Renfro
 */
@EnableTask
@Configuration
@EnableConfigurationProperties(TaskAppProperties.class)
public class TaskAppConfiguration {
	@Bean
	public CommandLineRunner commandLineRunner(TaskAppProperties props) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) {
				System.out.println("The exitMessage is " + props.getExitMessage());
			}
		};
	}

	@Bean
	public TaskAppAnnotations annotations() {
		return new TaskAppAnnotations();
	}
}
