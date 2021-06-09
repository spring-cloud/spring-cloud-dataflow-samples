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

import java.util.Collections;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
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
import org.springframework.integration.support.MutableMessage;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.integration.webflux.inbound.WebFluxInboundEndpoint;
import org.springframework.messaging.Message;

/**
 * @author Ilayaperumal Gopinathan
 */
@SpringBootApplication
public class HttpClicksAndRegionIngest {

	@Bean
	public Publisher<Message<byte[]>> clicksAndRegionsFlow() {
		Publisher<Message<byte[]>> publisher =  IntegrationFlows.from(
				WebFlux.inboundChannelAdapter("/clicks", "/regions")
						.requestPayloadType(byte[].class)
						.statusCodeExpression(new ValueExpression<>(HttpStatus.ACCEPTED))
						.mappedRequestHeaders("username")
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
	public Supplier<Flux<Message<Long>>> clickSupplier(Publisher<Message<byte[]>> clickSupplier,
			WebFluxInboundEndpoint webFluxInboundEndpoint) {

		return () -> Flux.from(clickSupplier)
				.filter(message -> ((String) message.getHeaders().get("http_requestUrl")).endsWith("/clicks"))
				.map(message -> { return toUserClicks(message);})
				.doOnSubscribe((subscription) -> webFluxInboundEndpoint.start())
				.doOnTerminate(webFluxInboundEndpoint::stop);
	}

	@Bean
	public Supplier<Flux<Message<String>>> regionSupplier(Publisher<Message<byte[]>> regionSupplier,
			WebFluxInboundEndpoint webFluxInboundEndpoint) {

		return () -> Flux.from(regionSupplier)
				.filter(message -> ((String) message.getHeaders().get("http_requestUrl")).endsWith("/regions"))
				.map(message -> { return toUserRegion(message); })
				.doOnSubscribe((subscription) -> webFluxInboundEndpoint.start())
				.doOnTerminate(webFluxInboundEndpoint::stop);
	}

	public Message<Long> toUserClicks(Message<byte[]> message) {
		return new MutableMessage<>(Long.valueOf(new String(message.getPayload())), Collections.singletonMap("username", message.getHeaders().get("username")));
	}

	public Message<String> toUserRegion(Message<byte[]> message) {
		return new MutableMessage<>(new String(message.getPayload()), Collections.singletonMap("username", message.getHeaders().get("username")));
	}

	public static void main(String[] args) {
		SpringApplication.run(HttpClicksAndRegionIngest.class, args);
	}
}
