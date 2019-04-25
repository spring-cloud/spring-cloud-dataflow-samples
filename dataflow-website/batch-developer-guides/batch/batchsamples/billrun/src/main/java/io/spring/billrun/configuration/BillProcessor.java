/*
 * Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.spring.billrun.configuration;

import io.spring.billrun.model.Bill;
import io.spring.billrun.model.Usage;

import org.springframework.batch.item.ItemProcessor;

public class BillProcessor implements ItemProcessor<Usage, Bill> {

	@Override
	public Bill process(Usage usage) {

		Double billAmount = usage.getDataUsage() * .001 + usage.getMinutes() * .01;
		return new Bill(usage.getId(), usage.getFirstName(), usage.getLastName(),
				usage.getDataUsage(), usage.getMinutes(), billAmount);
	}
}
