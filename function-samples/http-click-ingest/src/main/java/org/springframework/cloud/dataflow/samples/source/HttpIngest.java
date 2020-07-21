/*
 * Copyright 2019 the original author or authors.
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

package org.springframework.cloud.dataflow.samples.source;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
@Import({ org.springframework.cloud.fn.supplier.http.HttpSupplierConfiguration.class })
public class HttpIngest {

	@Bean
	public Function<Message<?>, Message<?>> byteArrayToLong() {
		return message -> {
			if (message.getPayload() instanceof byte[]) {
				MessageHeaders headers = message.getHeaders();
				String contentType = headers.containsKey("contentType") ?
						headers.get("contentType").toString() :
						"application/json";
				if (contentType.contains("text") || contentType.contains("json") || contentType
						.contains("x-spring-tuple")) {
					message = MessageBuilder
							.withPayload(Long.valueOf(new String((byte[]) ((byte[]) message.getPayload()))))
							.copyHeaders(message.getHeaders()).build();
				}
			}

			return message;
		};
	}

	@Bean
	public Function<Message<?>, Message<?>> byteArrayToString() {
		return message -> {
			if (message.getPayload() instanceof byte[]) {
				MessageHeaders headers = message.getHeaders();
				String contentType = headers.containsKey("contentType") ?
						headers.get("contentType").toString() :
						"application/json";
				if (contentType.contains("text") || contentType.contains("json") || contentType
						.contains("x-spring-tuple")) {
					message = MessageBuilder
							.withPayload(new String((byte[]) ((byte[]) message.getPayload())))
							.copyHeaders(message.getHeaders()).build();
				}
			}

			return message;
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(HttpIngest.class, args);
	}
}
