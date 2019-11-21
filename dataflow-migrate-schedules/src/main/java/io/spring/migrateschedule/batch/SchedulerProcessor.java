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

package io.spring.migrateschedule.batch;

import io.spring.migrateschedule.service.ConvertScheduleInfo;
import io.spring.migrateschedule.service.MigrateProperties;
import io.spring.migrateschedule.service.MigrateScheduleService;
import io.spring.migrateschedule.service.ScheduleProcessedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleInfo;

/**
 * Enriches the {@link ConvertScheduleInfo} with information obtained from the platform.
 * The new name for the schedule is established and the properties as well as commandline args
 * so that the SchedulerTaskLauncher can process the entries.
 *
 * @author Glenn Renfro
 */
public class SchedulerProcessor<T, C extends ScheduleInfo> implements ItemProcessor<ConvertScheduleInfo, ConvertScheduleInfo>{

	private static final Logger logger = LoggerFactory.getLogger(SchedulerProcessor.class);

	private MigrateScheduleService migrateScheduleService;

	private MigrateProperties migrateProperties;

	public SchedulerProcessor(MigrateScheduleService migrateScheduleService, MigrateProperties migrateProperties) {
		this.migrateScheduleService = migrateScheduleService;
		this.migrateProperties = migrateProperties;
	}

	@Override
	public ConvertScheduleInfo process(ConvertScheduleInfo scheduleInfo){
		if(scheduleInfo.getScheduleName().contains(migrateProperties.getSchedulerToken())) {
			throw new ScheduleProcessedException(scheduleInfo.getScheduleName());
		}
		logger.info(String.format("Processing Schedule %s", scheduleInfo.getScheduleName()));
		return this.migrateScheduleService.enrichScheduleMetadata(scheduleInfo);
	}
}
