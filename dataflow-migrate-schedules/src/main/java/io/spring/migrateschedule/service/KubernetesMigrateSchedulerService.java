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

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.deployer.resource.maven.MavenProperties;
import org.springframework.cloud.deployer.spi.scheduler.Scheduler;

/**
 * Services required to migrate Cron Job schedules to the 2.3.0 format and stage the
 * ShedulerTaskLauncher.
 *
 * @author Glenn Renfro
 */
public class KubernetesMigrateSchedulerService extends AbstractMigrateService {

	public KubernetesMigrateSchedulerService(MigrateProperties migrateProperties,
			TaskDefinitionRepository taskDefinitionRepository,
			MavenProperties mavenProperties) {
		super(migrateProperties, taskDefinitionRepository, mavenProperties);
	}
	@Override
	public List<ConvertScheduleInfo> scheduleInfoList() {
		return new ArrayList<>();
	}

	@Override
	public ConvertScheduleInfo enrichScheduleMetadata(ConvertScheduleInfo scheduleInfo) {
		return scheduleInfo;
	}

	@Override
	public void migrateSchedule(Scheduler scheduler, ConvertScheduleInfo scheduleInfo) {

	}
}
