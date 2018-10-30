/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.cloud.ingest.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * BatchConfiguration test cases
 *
 * @author Chris Schaefer
 */
public class BatchConfigurationTests {
	private static AnnotationConfigApplicationContext context;

	@Before
	public void createContext() {
		context = new AnnotationConfigApplicationContext(new Class[] {
				BatchConfiguration.class, BatchConfigurationTests.DataSourceConfiguration.class });
	}

	@After
	public void closeContext() {
		context.close();
	}

	@Test
	public void testBatchConfigurationSuccess() throws Exception {
		JobExecution jobExecution = testJob("classpath:data.csv");

		assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());
	}

	@Test
	public void testBatchConfigurationFail() throws Exception {
		JobExecution jobExecution = testJob("classpath:missing-data-file.csv");

		assertEquals("Incorrect batch status", BatchStatus.FAILED, jobExecution.getStatus());
	}

	@Test
	public void testBatchDataProcessing() throws Exception {
		JobExecution jobExecution = testJob("classpath:data.csv");

		assertEquals("Incorrect batch status", BatchStatus.COMPLETED, jobExecution.getStatus());
		assertEquals("Invalid number of step executions", 1, jobExecution.getStepExecutions().size());

		JdbcTemplate jdbcTemplate = new JdbcTemplate(context.getBean(DataSource.class));
		List<Map<String, Object>> peopleList = jdbcTemplate.queryForList("select first_name, last_name from people");

		assertEquals("Incorrect number of results", 5, peopleList.size());

		for(Map<String, Object> person : peopleList) {
			assertNotNull("Received null person", person);

			String firstName = (String) person.get("first_name");
			assertEquals("Invalid first name: " + firstName, firstName.toUpperCase(), firstName);

			String lastName = (String) person.get("last_name");
			assertEquals("Invalid last name: " + lastName, lastName.toUpperCase(), lastName);
		}
	}

	private JobExecution testJob(String filePath) throws Exception {
		Job job = context.getBean(Job.class);
		JobLauncher jobLauncher = context.getBean(JobLauncher.class);
		JobParameters jobParameters = new JobParametersBuilder()
			.addString("filePath", filePath)
			.toJobParameters();

		return jobLauncher.run(job, jobParameters);
	}

	@Configuration
	public static class DataSourceConfiguration {
		@Autowired
		private ResourceLoader resourceLoader;

		@PostConstruct
		protected void initialize() {
			ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
			populator.addScript(resourceLoader.getResource(ClassUtils.addResourcePathToPackagePath(Step.class, "schema-hsqldb.sql")));
			populator.addScript(resourceLoader.getResource("classpath:schema-all.sql"));
			populator.setContinueOnError(true);
			DatabasePopulatorUtils.execute(populator, dataSource());
		}

		@Bean
		public DataSource dataSource() {
			return new EmbeddedDatabaseFactory().getDatabase();
		}
	}
}
