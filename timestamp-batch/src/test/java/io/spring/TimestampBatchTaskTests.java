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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the Task Application outputs the correct Batch and Task log entries.
 *
 * @author Glenn Renfro
 */
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
public class TimestampBatchTaskTests {

	@Test
	public void testTimeStampApp(CapturedOutput capturedOutput) throws Exception {
		final String TEST_DATE_DOTS = ".......";
		final String CREATE_TASK_MESSAGE = "Creating: TaskExecution{executionId=";
		final String UPDATE_TASK_MESSAGE = "Updating: TaskExecution with executionId=1 with the following";
		final String JOB1_MESSAGE = "Job1 was run with date ";
		final String JOB2_MESSAGE = "Job2 was run with date ";

		String[] args = {"--timestamp.format=yyyy" + TEST_DATE_DOTS};

		SpringApplication
				.run(TestTimestampBatchTaskApplication.class, args);

		String output = capturedOutput.toString();

		assertThat(output).contains(TEST_DATE_DOTS);
		assertThat(output).contains(CREATE_TASK_MESSAGE);
		assertThat(output).contains(UPDATE_TASK_MESSAGE);

		assertThat(output).contains(JOB1_MESSAGE);
		assertThat(output).contains(JOB2_MESSAGE);

	}


	@SpringBootApplication
	public static class TestTimestampBatchTaskApplication {
		public static void main(String[] args) {
			SpringApplication.run(TestTimestampBatchTaskApplication.class, args);
		}
	}

}
