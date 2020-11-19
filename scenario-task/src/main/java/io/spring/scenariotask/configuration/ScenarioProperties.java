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

package io.spring.scenariotask.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Properties that allow the user to specify how they want the Batch/Task app to behave for a test.
 *
 * @author Glenn Renfro
 */
@ConfigurationProperties(prefix = "io.spring")
public class ScenarioProperties {

	/**
	 * The name associated with the batch job.  The default is "scenario-job".
	 */
	private String jobName = "scenario-job";

	/**
	 * The name associated with the single step for the job.  The default is "scenario-step".
	 */
	private String stepName = "scenario-step";

	/**
	 * If true, the batch will throw a {@link ExpectedException}.  Defaults to false.
	 */
	private boolean failBatch;

	/**
	 * If true, the task will throw a {@link ExpectedException}.  Defaults to false.
	 */
	private boolean failTask;

	/**
	 * If true, the task will launch a sample batch job.  Defaults to true.
	 */
	private boolean launchBatchJob = true;

	/**
	 * How long the batch job should pause in the step.  Defaults to 0.
	 */
	private int pauseInSeconds = 0;

	/**
	 * If true a runIdIncrementer will be applied to the batch job.  Defaults to false.
	 */
	private boolean includeRunidIncrementer;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public boolean isFailBatch() {
		return failBatch;
	}

	public void setFailBatch(boolean failBatch) {
		this.failBatch = failBatch;
	}

	public boolean isFailTask() {
		return failTask;
	}

	public void setFailTask(boolean failTask) {
		this.failTask = failTask;
	}

	public int getPauseInSeconds() {
		return pauseInSeconds;
	}

	public void setPauseInSeconds(int pauseInSeconds) {
		this.pauseInSeconds = pauseInSeconds;
	}

	public String getStepName() {
		return stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	public boolean isLaunchBatchJob() {
		return launchBatchJob;
	}

	public void setLaunchBatchJob(boolean launchBatchJob) {
		this.launchBatchJob = launchBatchJob;
	}

	public boolean isIncludeRunidIncrementer() {
		return includeRunidIncrementer;
	}

	public void setIncludeRunidIncrementer(boolean includeRunidIncrementer) {
		this.includeRunidIncrementer = includeRunidIncrementer;
	}
}
