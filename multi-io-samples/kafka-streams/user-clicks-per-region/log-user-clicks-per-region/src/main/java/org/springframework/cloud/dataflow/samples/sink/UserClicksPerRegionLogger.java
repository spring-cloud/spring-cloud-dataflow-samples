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

package org.springframework.cloud.dataflow.samples.sink;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;

@SpringBootApplication
public class UserClicksPerRegionLogger {

	@Bean
	IntegrationFlow logConsumerFlow() {
		return IntegrationFlows.from(MessageConsumer.class, (gateway) -> gateway.beanName("logConsumer"))
				.handle((payload, headers) -> {
					String userRegion = (String) headers.get("kafka_receivedMessageKey");
					return (userRegion + ": " + payload);
				})
				.log(LoggingHandler.Level.INFO, "user-clicks-per-region", "payload")
				.get();
	}

	private interface MessageConsumer extends Consumer<Message<?>> {
	}

	public static void main(String[] args) {
		SpringApplication.run(UserClicksPerRegionLogger.class, args);
	}
}
