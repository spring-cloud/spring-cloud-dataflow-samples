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

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.integration.webflux.inbound.WebFluxInboundEndpoint;
import org.springframework.messaging.Message;

/**
 * @author Ilayaperumal Gopinathan
 */
@SpringBootApplication
public class PurchaseOrderIngest {

	private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderIngest.class);

	@Bean
	public Publisher<Message<byte[]>> purchaseOrders() {
		Publisher<Message<byte[]>> publisher =  IntegrationFlows.from(
				WebFlux.inboundChannelAdapter("/")
						.requestPayloadType(byte[].class)
						.statusCodeExpression(new ValueExpression<>(HttpStatus.ACCEPTED))
						.autoStartup(false))
				.channel(MessageChannels.flux())
				.toReactivePublisher();
		return publisher;
	}

	@Bean
	public HeaderMapper<HttpHeaders> httpHeaderMapper() {
		return DefaultHttpHeaderMapper.inboundMapper();
	}

	@Bean
	public Supplier<Flux<Message<byte[]>>> debitTransactions(Publisher<Message<byte[]>> purchaseOrders,
			WebFluxInboundEndpoint webFluxInboundEndpoint) {

		return () -> Flux.from(purchaseOrders)
				.filter(message ->  filterTransactions(message, "debit"))
				.doOnSubscribe((subscription) -> webFluxInboundEndpoint.start())
				.doOnTerminate(webFluxInboundEndpoint::stop);
	}

	@Bean
	public Supplier<Flux<Message<byte[]>>> creditTransactions(Publisher<Message<byte[]>> purchaseOrders,
			WebFluxInboundEndpoint webFluxInboundEndpoint) {

		return () -> Flux.from(purchaseOrders)
				.filter(message ->  filterTransactions(message, "credit"))
				.doOnSubscribe((subscription) -> webFluxInboundEndpoint.start())
				.doOnTerminate(webFluxInboundEndpoint::stop);
	}

	@Bean
	public Supplier<Flux<Message<byte[]>>> cashTransactions(Publisher<Message<byte[]>> purchaseOrders,
			WebFluxInboundEndpoint webFluxInboundEndpoint) {


		return () -> Flux.from(purchaseOrders)
				.filter(message ->  filterTransactions(message, "cash"))
				.doOnSubscribe((subscription) -> webFluxInboundEndpoint.start())
				.doOnTerminate(webFluxInboundEndpoint::stop);
	}

	public boolean filterTransactions(Message<byte[]> message, String mode)  {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, String> paylodMap = objectMapper.readValue(message.getPayload(), Map.class);
			return paylodMap.get("mode").equals(mode);
		}
		catch (IOException e) {
			logger.error("Error parsing the payload: " + e.getStackTrace());
		}
		return false;
	}

	public static void main(String[] args) {
		SpringApplication.run(PurchaseOrderIngest.class, args);
	}
}
