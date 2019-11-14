/*
 * Copyright 2019 the original author or authors.
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

package io.spring.migrateschedule.service;

/**
 * Allows user to configure the migration application.
 *
 * @author Glenn Renfro
 */
public class MigrateProperties {
	private String schedulerTaskLauncherUrl = "maven://org.springframework.cloud:spring-cloud-dataflow-scheduler-task-launcher:2.3.0.BUILD-SNAPSHOT";

	/**
	 * The token for the updated schedules.
	 */
	private String schedulerToken = "scdf-";

	/**
	 * The prefix to attach to the application properties to be sent to the SchedulerTaskLauncher.
	 */
	private String taskLauncherPrefix = "tasklauncher";

	private String dataflowServerUri = "http://localhost:9393";

	/**
	 * The global Java Options required for the applications to be launched by the schedulerTaskLauncher.
	 */
	private String javaOptions;

	/**
	 * The global timeout to be assigned to applications to be launched by the schedulerTaskLauncher.
	 */
	private String healthCheckTimeout;

	/**
	 * The global api timeout to be assigned to applications to be launched by scheduleTaskLauncher.
	 */
	private Long apiTimeout;

	/**
	 * Timeout for status API operations in milliseconds to be assigned to applications to be launched by scheduleTaskLauncher
	 */
	private Long statusTimeout;

	/**
	 * If set, the global override the timeout allocated for staging an app launched by the schedulefTaskLauncher.
	 */
	private Long stagingTimeout;

	/**
	 * If set, the global override the timeout allocated for starting an app launched by scheduleTaskLauncher.
	 */
	private Long startupTimeout;

	/**
	 * If set, the global override for the maximum number of concurrently running tasks.
	 */
	private Integer maximumConcurrentTasks;

	/**
	 * The number of seconds to wait for a schedule to complete.
	 * This excludes the time it takes to stage the application on Cloud Foundry.
	 */
	private int scheduleTimeoutInSeconds = 30;


	public String getSchedulerTaskLauncherUrl() {
		return schedulerTaskLauncherUrl;
	}

	public void setSchedulerTaskLauncherUrl(String schedulerTaskLauncherUrl) {
		this.schedulerTaskLauncherUrl = schedulerTaskLauncherUrl;
	}

	public String getSchedulerToken() {
		return schedulerToken;
	}

	public void setSchedulerToken(String schedulerToken) {
		this.schedulerToken = schedulerToken;
	}

	public String getTaskLauncherPrefix() {
		return taskLauncherPrefix;
	}

	public void setTaskLauncherPrefix(String taskLauncherPrefix) {
		this.taskLauncherPrefix = taskLauncherPrefix;
	}

	public String getDataflowServerUri() {
		return dataflowServerUri;
	}

	public void setDataflowServerUri(String dataflowServerUri) {
		this.dataflowServerUri = dataflowServerUri;
	}

	public int getScheduleTimeoutInSeconds() {
		return scheduleTimeoutInSeconds;
	}

	public void setScheduleTimeoutInSeconds(int scheduleTimeoutInSeconds) {
		this.scheduleTimeoutInSeconds = scheduleTimeoutInSeconds;
	}

	public String getJavaOptions() {
		return javaOptions;
	}

	public void setJavaOptions(String javaOptions) {
		this.javaOptions = javaOptions;
	}

	public String getHealthCheckTimeout() {
		return healthCheckTimeout;
	}

	public void setHealthCheckTimeout(String healthCheckTimeout) {
		this.healthCheckTimeout = healthCheckTimeout;
	}

	public Long getApiTimeout() {
		return apiTimeout;
	}

	public void setApiTimeout(Long apiTimeout) {
		this.apiTimeout = apiTimeout;
	}

	public Long getStatusTimeout() {
		return statusTimeout;
	}

	public void setStatusTimeout(Long statusTimeout) {
		this.statusTimeout = statusTimeout;
	}

	public Long getStagingTimeout() {
		return stagingTimeout;
	}

	public void setStagingTimeout(Long stagingTimeout) {
		this.stagingTimeout = stagingTimeout;
	}

	public Long getStartupTimeout() {
		return startupTimeout;
	}

	public void setStartupTimeout(Long startupTimeout) {
		this.startupTimeout = startupTimeout;
	}

	public Integer getMaximumConcurrentTasks() {
		return maximumConcurrentTasks;
	}

	public void setMaximumConcurrentTasks(Integer maximumConcurrentTasks) {
		this.maximumConcurrentTasks = maximumConcurrentTasks;
	}
}
