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
import io.spring.migrateschedule.service.MigrateScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

/**
 * Enriches the ConvertScheduleInfo with information obtained from the platform.
 * The new name for the schedule is established and the properties as well as commandline args
 * so that the SchedulerTaskLauncher can process the entries.
 *
 * @param <T> The  ConvertSchedulerInfo class.
 *
 * @author Glenn Renfro
 */
public class SchedulerProcessor<T> implements ItemProcessor {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerProcessor.class);

	private MigrateScheduleService migrateScheduleService;

	public SchedulerProcessor(MigrateScheduleService migrateScheduleService) {
		this.migrateScheduleService = migrateScheduleService;
	}

	@Override
	public Object process(Object o){
		ConvertScheduleInfo scheduleInfo = (ConvertScheduleInfo) o;
		logger.info(String.format("Processing Schedule %s", scheduleInfo.getScheduleName()));
		return this.migrateScheduleService.enrichScheduleMetadata(scheduleInfo);
	}

}
