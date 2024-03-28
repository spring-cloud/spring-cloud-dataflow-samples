/*
 * Copyright 2022 the original author or authors.
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
package io.spring.scdf.waitron;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Corneil du Plessis
 */
@Configuration
public class Waitron {
    private static final Logger logger = LoggerFactory.getLogger(Waitron.class);
    private final StreamBridge bridge;
    private final ApplicationAvailability applicationAvailability;
    private final Environment environment;
    private final AsyncTaskExecutor taskExecutor;

    public Waitron(StreamBridge bridge, ApplicationAvailability applicationAvailability, Environment environment, AsyncTaskExecutor taskExecutor) {
        this.bridge = bridge;
        this.applicationAvailability = applicationAvailability;
        this.environment = environment;
        this.taskExecutor = taskExecutor;
    }

    @Bean(name = Events.PAYMENT)
    public Consumer<String> acceptPayment() {
        return (String payment) -> {
            logger.info("acceptPayment:{}", payment);
        };
    }

    @EventListener
    public void onEvent(AvailabilityChangeEvent<ReadinessState> event) {
        logger.info("onEvent:{}", event.getState());
        logger.info("availability:{}:{}", applicationAvailability.getLivenessState(), applicationAvailability.getReadinessState());
        if (!environment.acceptsProfiles(Profiles.of("test"))) {
            if (LivenessState.CORRECT.equals(applicationAvailability.getLivenessState())) {
                taskExecutor.submit((Callable<Object>) () -> {
                    logger.info("onEvent:wait");
                    Thread.sleep(30000L);
                    logger.info("onEvent:send:atWork:waitron");
                    return bridge.send(Events.AT_WORK, MessageBuilder.withPayload("waitron").build());
                });
            }
        } else {
            logger.info("onEvent:skip:test");
        }
    }

    @Bean(name = Events.FOOD)
    public Consumer<String> collectFood() {
        return (String food) -> {
            logger.info("collectFood:send:delivery:{}", food);
            bridge.send(Events.DELIVERY, MessageBuilder.withPayload(food).build());
        };
    }

    @Bean(name = Events.HOT_DRINKS)
    public Consumer<String> collectHotDrinks() {
        return (String hotDrinks) -> {
            logger.info("collectHotDrinks:send:delivery:{}", hotDrinks);
            bridge.send(Events.DELIVERY, MessageBuilder.withPayload(hotDrinks).build());
        };
    }

    @Bean(name = Events.COLD_DRINKS)
    public Consumer<String> collectColdDrinks() {
        return (String coldDrinks) -> {
            logger.info("collectColdDrinks:send:delivery:{}", coldDrinks);
            bridge.send(Events.DELIVERY, MessageBuilder.withPayload(coldDrinks).build());
        };
    }

    @Bean(name = Events.ORDER)
    public Consumer<String> acceptOrders() {
        return (String order) -> {
            logger.info("acceptOrder:send:orders:{}", order);
            bridge.send(Events.ORDERS, MessageBuilder.withPayload(order).build());
        };
    }

    public interface Events {
        String ORDERS = "orders";
        String ORDER = "order";
        String FOOD = "food";
        String COLD_DRINKS = "coldDrinks";
        String HOT_DRINKS = "hotDrinks";
        String PAYMENT = "payment";

        String AT_WORK = "at_work";
        String DELIVERY = "delivery";
    }
}
