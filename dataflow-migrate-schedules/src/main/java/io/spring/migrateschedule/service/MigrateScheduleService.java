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

import java.util.List;

import org.springframework.cloud.dataflow.core.TaskDefinition;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleInfo;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;

/**
 * Interface that establishes the method signatures required to migrate
 * schedules to the 2.3.0 format as well as stage the application.
 */
public interface MigrateScheduleService {

	/**
	 * Retrieve all available {@link ScheduleInfo}s.
	 * @return list of available ScheduleInfos
	 */
	List<ConvertScheduleInfo> scheduleInfoList();

	/**
	 * Add properties and commandLine args to the {@link ScheduleInfo}
	 * @return enriched {@link ConvertScheduleInfo}
	 */
	ConvertScheduleInfo enrichScheduleMetadata(ConvertScheduleInfo scheduleInfo);

	/**
	 * Migrates existing schedule to new SCDF schedule.
	 * @param scheduler the deployer scheduler to build the new schedule.
	 * @param scheduleInfo the schedule info containing the existing schedule.
	 */
	void migrateSchedule(Scheduler scheduler, ConvertScheduleInfo scheduleInfo);

	/**
	 * Retrieve {@link TaskDefinition} for the name provided
	 * @param taskDefinitionName the name of the {@link TaskDefinition}.
	 * @return a TaskDefinition
	 */
	TaskDefinition findTaskDefinitionByName(String taskDefinitionName);
}
