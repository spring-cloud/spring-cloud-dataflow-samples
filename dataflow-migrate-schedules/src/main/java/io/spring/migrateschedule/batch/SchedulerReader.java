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

import java.util.Collections;
import java.util.List;

import io.jsonwebtoken.lang.Assert;
import io.spring.migrateschedule.service.ConvertScheduleInfo;
import io.spring.migrateschedule.service.MigrateScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.cloud.deployer.spi.scheduler.ScheduleInfo;
import org.springframework.util.StringUtils;

/**
 * Retrieves all of the available schedules.
 *
 * @param <T> The  ConvertSchedulerInfo class.
 *
 * @author Glenn Renfro
 */
public class SchedulerReader<T> implements ItemReader {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerReader.class);

	private List<ConvertScheduleInfo> scheduleInfoList;

	private int currentOffset;

	private int scheduleCount;

	private StepExecution stepExecution;

	private boolean initialized;

	public SchedulerReader(MigrateScheduleService migrateScheduleService) {
		Assert.notNull(migrateScheduleService, "convertScheduleService must not be null");
		logger.info("Retrieving schedules from PCF Scheduler");
		this.scheduleInfoList = migrateScheduleService.scheduleInfoList();
		this.scheduleCount = this.scheduleInfoList.size();
	}

	@Override
	public Object read() {
		if(!this.initialized) {
			this.currentOffset = getCurrentOffset();
			this.initialized = true;
		}
		ScheduleInfo result = null;
		if(this.currentOffset < this.scheduleCount) {
			result =  this.scheduleInfoList.get(this.currentOffset++);
		}
		return result;
	}



	private int getCurrentOffset() {
		int result = 0;
		Collections.sort(this.scheduleInfoList);
		String lastSuccessfulScheduleName = (String)this.stepExecution.getExecutionContext().get("scheduleName");
		if (StringUtils.hasText(lastSuccessfulScheduleName) && this.scheduleCount != 0) {
			for(; result < this.scheduleCount; result++) {
				ScheduleInfo scheduleInfo =  this.scheduleInfoList.get(result);
				logger.info(String.format("Skipping %s, it has already been processed", scheduleInfo.getScheduleName()));
				if(scheduleInfo.getScheduleName().equals(lastSuccessfulScheduleName)) {
					result++;
					break;
				}
			}
		}
		 return result;
	}

	@BeforeStep
	public void saveStepExecution(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}
}
