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

package io.spring.migrateschedule.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows user to configure the migration application.
 *
 * @author Glenn Renfro
 */
public class MigrateProperties {
	/**
	 * The token for the updated schedules.
	 */
	private String schedulerToken = "scdf-";

	/**
	 * The prefix to attach to the application properties to be sent to the SchedulerTaskLauncher.
	 */
	private String taskLauncherPrefix = "tasklauncher";

	/**
	 * Comma delimited list of schedules to migrate.  If empty then all schedules will be migrated.
	 */
	private List<String> scheduleNamesToMigrate = new ArrayList<>();


	/**
	 * The registered application name for the composed task runner.
	 * Update this if the dataflow server use has a different composed task runner app name than the default.
	 */
	private String composedTaskRunnerRegisteredAppName = "composed-task-runner";

	/**
	 * The user name of the database that the migrated task will use.
	 */
	private String dbUserName;

	/**
	 * The password of the database that the migrated task will use.
	 */
	private String dbPassword;

	/**
	 * The url to the database that the migrated task will use.
	 */
	private String dbUrl;

	/**
	 * The driver class name to use for the database that the migrated task will use.
	 */
	private String dbDriverClassName;

	/**
	 * The url of the Spring Cloud Data Flow Server that migrated composed task runners should execute task launch commands.
	 */
	private String dataflowUrl="http://localhost:9393";

	/**
	 * Establish the name of the service account for the schedule.
	 */
	private String taskServiceAccountName = "default";

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

	public List<String> getScheduleNamesToMigrate() {
		return scheduleNamesToMigrate;
	}

	public void setScheduleNamesToMigrate(List<String> scheduleNamesToMigrate) {
		this.scheduleNamesToMigrate = scheduleNamesToMigrate;
	}

	public String getDbUserName() {
		return dbUserName;
	}

	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbUrl() {
		return dbUrl;
	}

	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}

	public String getDbDriverClassName() {
		return dbDriverClassName;
	}

	public void setDbDriverClassName(String dbDriverClassName) {
		this.dbDriverClassName = dbDriverClassName;
	}

	public String getComposedTaskRunnerRegisteredAppName() {
		return composedTaskRunnerRegisteredAppName;
	}

	public void setComposedTaskRunnerRegisteredAppName(String composedTaskRunnerRegisteredAppName) {
		this.composedTaskRunnerRegisteredAppName = composedTaskRunnerRegisteredAppName;
	}

	public String getDataflowUrl() {
		return dataflowUrl;
	}

	public void setDataflowUrl(String dataflowUrl) {
		this.dataflowUrl = dataflowUrl;
	}

	public String getTaskServiceAccountName() {
		return taskServiceAccountName;
	}

	public void setTaskServiceAccountName(String taskServiceAccountName) {
		this.taskServiceAccountName = taskServiceAccountName;
	}
}
