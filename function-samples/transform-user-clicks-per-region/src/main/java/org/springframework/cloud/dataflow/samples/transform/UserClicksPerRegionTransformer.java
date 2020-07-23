/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.dataflow.samples.transform;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;

@SpringBootApplication
public class UserClicksPerRegionTransformer {


	@Bean
	public Function<Message<?>, Message<UserClicksPerRegion>> transform() {
		return message -> {
			String regions = (String) message.getHeaders().get("kafka_receivedMessageKey");
			Long clicks = (Long) message.getPayload();
			return new MutableMessage<>(new UserClicksPerRegion(regions, clicks));
		};
	}

	public class UserClicksPerRegion  {

		private String regions;

		public UserClicksPerRegion(String regions, Long clicks) {
			this.regions = regions;
			this.clicks = clicks;
		}

		private Long clicks;

		public String getRegions() {
			return regions;
		}

		public Long getClicks() {
			return clicks;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(UserClicksPerRegionTransformer.class, args);
	}
}
