/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.cloud.dataflow.ingest;

import java.util.List;
import java.util.Map;

import io.spring.cloud.dataflow.ingest.config.BatchConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * BatchConfiguration test cases
 *
 * @author Chris Schaefer
 * @author David Turanski
 */

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { BatchConfiguration.class, BatchApplicationTests.BatchTestConfiguration.class })
public class BatchApplicationTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void testBatchConfigurationFail() throws Exception {

		BatchStatus status = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addString(
			"localFilePath", "classpath:missing-data.csv").toJobParameters()).getStatus();
		assertEquals("Incorrect batch status", BatchStatus.FAILED, status);
	}

	@Test
	public void testBatchDataProcessing() throws Exception {

		JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addString(
			"localFilePath", "classpath:data.csv").toJobParameters());

		assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());

		assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());

		List<Map<String, Object>> peopleList = jdbcTemplate.queryForList(
			"select first_name, last_name from people");

		assertEquals("Incorrect number of results", 5, peopleList.size());

		for (Map<String, Object> person : peopleList) {
			assertNotNull("Received null person", person);

			String firstName = (String) person.get("first_name");
			assertEquals("Invalid first name: " + firstName, firstName.toUpperCase(), firstName);

			String lastName = (String) person.get("last_name");
			assertEquals("Invalid last name: " + lastName, lastName.toUpperCase(), lastName);
		}
	}

	@Configuration
	@EnableAutoConfiguration
	public static class BatchTestConfiguration {

		@Bean
		public JobLauncherTestUtils jobLauncherTestUtils() {
			return new JobLauncherTestUtils();
		}
	}
}
