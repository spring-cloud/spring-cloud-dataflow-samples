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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.log.sink.kafka;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.fn.consumer.log.LogConsumerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;

@SpringBootApplication
@Import({ org.springframework.cloud.fn.consumer.log.LogConsumerConfiguration.class })
public class MultiInputLogSinkKafkaApplication {

	@Bean
	IntegrationFlow logConsumerFlow2(LogConsumerProperties logSinkProperties) {
		return IntegrationFlow.from(MessageConsumer.class, (gateway) -> gateway.beanName("logConsumer2"))
				.handle((payload, headers) -> payload)
				.log(logSinkProperties.getLevel(), logSinkProperties.getName(), logSinkProperties.getExpression())
				.get();
	}

	@Bean
	IntegrationFlow logConsumerFlow3(LogConsumerProperties logSinkProperties) {
		return IntegrationFlow.from(MessageConsumer.class, (gateway) -> gateway.beanName("logConsumer3"))
				.handle((payload, headers) -> payload)
				.log(logSinkProperties.getLevel(), logSinkProperties.getName(), logSinkProperties.getExpression())
				.get();
	}

	private interface MessageConsumer extends Consumer<Message<?>> {

	}

	public static void main(String[] args) {
		SpringApplication.run(MultiInputLogSinkKafkaApplication.class, args);
	}
}
