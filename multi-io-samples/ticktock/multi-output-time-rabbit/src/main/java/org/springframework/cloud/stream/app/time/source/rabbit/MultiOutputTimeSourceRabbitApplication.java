/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is dMuistributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.time.source.rabbit;

import java.util.Date;
import java.util.function.Supplier;

import org.apache.commons.lang3.time.FastDateFormat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.fn.supplier.time.TimeSupplierProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import({ org.springframework.cloud.fn.supplier.time.TimeSupplierConfiguration.class })
public class MultiOutputTimeSourceRabbitApplication {

	@Bean
	public Supplier<String> timeSupplier2(TimeSupplierProperties timeSupplierProperties) {
		FastDateFormat fastDateFormat = FastDateFormat.getInstance(timeSupplierProperties.getDateFormat());
		return () -> fastDateFormat.format(new Date());
	}

	@Bean
	public Supplier<String> timeSupplier3(TimeSupplierProperties timeSupplierProperties) {
		FastDateFormat fastDateFormat = FastDateFormat.getInstance(timeSupplierProperties.getDateFormat());
		return () -> fastDateFormat.format(new Date());
	}

	public static void main(String[] args) {
		SpringApplication.run(MultiOutputTimeSourceRabbitApplication.class, args);
	}
}
