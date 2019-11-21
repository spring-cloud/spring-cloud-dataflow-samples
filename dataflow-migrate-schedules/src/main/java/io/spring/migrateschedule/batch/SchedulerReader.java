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

import java.util.List;

import io.spring.migrateschedule.service.ConvertScheduleInfo;
import io.spring.migrateschedule.service.MigrateScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleInfo;
import org.springframework.util.Assert;

/**
 * Retrieves all of the available schedules.
 *
 * @author Glenn Renfro
 */
public class SchedulerReader<C extends ScheduleInfo> extends AbstractItemCountingItemStreamItemReader<ConvertScheduleInfo> {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerReader.class);

	private List<ConvertScheduleInfo> scheduleInfoList;

	private MigrateScheduleService migrateScheduleService;

	public SchedulerReader(MigrateScheduleService migrateScheduleService) {
		Assert.notNull(migrateScheduleService, "convertScheduleService must not be null");
		logger.info("Retrieving schedules from PCF Scheduler");
		this.migrateScheduleService = migrateScheduleService;
	}

	@Override
	protected ConvertScheduleInfo doRead(){
		  return this.scheduleInfoList.get(this.getCurrentItemCount()-1);
	}

	@Override
	protected void doOpen() {
		this.scheduleInfoList = migrateScheduleService.scheduleInfoList();
		this.setMaxItemCount(this.scheduleInfoList.size());
		this.setName("scheduler-reader");
	}

	@Override
	protected void doClose(){

	}
}
