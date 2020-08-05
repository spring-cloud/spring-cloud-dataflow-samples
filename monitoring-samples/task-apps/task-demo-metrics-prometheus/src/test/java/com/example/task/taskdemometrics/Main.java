package com.example.task.taskdemometrics;

import java.time.Duration;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.prometheus.rsocket.PrometheusRSocketClient;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import reactor.util.retry.Retry;

/**
 * @author Christian Tzolov
 */
public class Main {


	public static void main(String[] args) throws InterruptedException {

		PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

		//PrometheusRSocketClient client = new PrometheusRSocketClient(meterRegistry,
		//		TcpClientTransport.create("localhost", 7001),
		//		c -> c.retryBackoff(Long.MAX_VALUE, Duration.ofSeconds(10), Duration.ofMinutes(10)));
		PrometheusRSocketClient client = PrometheusRSocketClient
				//.build(meterRegistry, WebsocketClientTransport.create("localhost", 8086))
				.build(meterRegistry, TcpClientTransport.create("localhost", 7001))
				.retry(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(10)).maxBackoff(Duration.ofMinutes(10)))
				.connect();

		LongTaskTimer longTaskTimer = LongTaskTimer
				.builder("tzolov.long")
				.description("Long task duration")
				.tag("task.my.tag", "0")
				.register(meterRegistry);

		LongTaskTimer.Sample longTaskSample = longTaskTimer.start();

		Timer.Sample taskSample = Timer.start(meterRegistry);
		//Timer.Sample taskSample = Timer.start();

		//Thread.sleep(2 * 60 * 1000);
		Thread.sleep(10 * 1000);

		longTaskSample.stop();

		taskSample.stop(Timer.builder("tzolov.task")
				.description("Task duration")
				.tag("task.exit.code", "0")
				.tag("task.exception", "none")
				.tag("task.status", "success")
				.register(meterRegistry));

		client.pushAndClose();

		//Thread.sleep(10000);

	}
}
