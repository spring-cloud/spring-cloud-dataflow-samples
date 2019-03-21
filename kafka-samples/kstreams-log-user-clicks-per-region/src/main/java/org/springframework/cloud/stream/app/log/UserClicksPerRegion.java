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

package org.springframework.cloud.stream.app.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.support.MutableMessage;
import org.springframework.messaging.Message;

@SpringBootApplication
public class UserClicksPerRegion {

	@EnableBinding(Sink.class)
	public static class Logger {

		@Bean
		@ServiceActivator(inputChannel = Sink.INPUT)
		public LoggingHandler logSinkHandler() {
			LoggingHandler loggingHandler = new LoggingHandler(LoggingHandler.Level.INFO) {

				@Override
				protected void handleMessageInternal(Message<?> message) throws Exception {
					Long userClicksPerRegion = (Long) message.getPayload();
					String userRegion = (String) message.getHeaders().get("kafka_receivedMessageKey");
					message = new MutableMessage<>(userRegion + " : " + userClicksPerRegion.toString());
					super.handleMessageInternal(message);
				}
			};
			return loggingHandler;
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(UserClicksPerRegion.class, args);
	}
}
