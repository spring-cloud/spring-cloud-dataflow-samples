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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Corneil du Plessis
 */
@Component
@EnableBinding(Waitron.Events.class)
public class Waitron {
    private static final Logger logger = LoggerFactory.getLogger(Waitron.class);
    private final Events events;
    private final ApplicationAvailability applicationAvailability;
    private final Environment environment;

    public Waitron(Events events, ApplicationAvailability applicationAvailability, Environment environment) {
        this.events = events;
        this.applicationAvailability = applicationAvailability;
        this.environment = environment;
    }

    @StreamListener(Events.PAYMENT)
    public void acceptPayment(String payment) {
        logger.info("acceptPayment:{}", payment);
    }

    @EventListener
    public void onEvent(AvailabilityChangeEvent<ReadinessState> event) {
        logger.info("onEvent:{}", event.getState());
        logger.info("availability:{}:{}", applicationAvailability.getLivenessState(), applicationAvailability.getReadinessState());
        if (!environment.acceptsProfiles(Profiles.of("test"))) {
            if (LivenessState.CORRECT.equals(applicationAvailability.getLivenessState())) {
                logger.info("onEvent:send:atWork:waitron");
                events.atWork().send(MessageBuilder.withPayload("waitron").build());
            }
        } else {
            logger.info("onEvent:skip:test");
        }
    }

    @StreamListener(Events.FOOD)
    public void collectFood(String food) {
        logger.info("collectFood:send:delivery:{}", food);
        events.delivery().send(MessageBuilder.withPayload(food).build());
    }

    @StreamListener(Events.HOT_DRINKS)
    public void collectHotDrinks(String hotDrinks) {
        logger.info("collectHotDrinks:send:delivery:{}", hotDrinks);
        events.delivery().send(MessageBuilder.withPayload(hotDrinks).build());
    }

    @StreamListener(Events.COLD_DRINKS)
    public void collectColdDrinks(String coldDrinks) {
        logger.info("collectColdDrinks:send:delivery:{}", coldDrinks);
        events.delivery().send(MessageBuilder.withPayload(coldDrinks).build());
    }

    @StreamListener(Events.ORDER)
    public void acceptOrders(String order) {
        logger.info("acceptOrder:send:orders:{}", order);
        events.orders().send(MessageBuilder.withPayload(order).build());
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

        @Input(PAYMENT)
        SubscribableChannel payment();

        @Input(ORDER)
        SubscribableChannel order();

        @Output(ORDERS)
        MessageChannel orders();

        @Output(DELIVERY)
        MessageChannel delivery();

        @Output(AT_WORK)
        MessageChannel atWork();

        @Input(FOOD)
        SubscribableChannel food();

        @Input(COLD_DRINKS)
        SubscribableChannel coldDrinks();

        @Input(HOT_DRINKS)
        SubscribableChannel hotDrinks();
    }
}
