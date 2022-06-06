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
package io.spring.scdf.restaurant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Corneil du Plessis
 */
@EnableBinding(Kitchen.Events.class)
@Component
public class Kitchen {
    private static final Logger logger = LoggerFactory.getLogger(Kitchen.class);
    private final Events events;

    public Kitchen(Events events) {
        this.events = events;
    }

    @StreamListener(Events.ORDERS)
    public void acceptOrders(String order) {
        logger.info("acceptOrder:{}", order);
        switch (order.toUpperCase()) {
            case "COFFEE":
            case "TEA":
                logger.info("dispense:send:hotDrinks:{}", order);
                events.hotDrinks().send(MessageBuilder.withPayload(order).build());
                break;
            case "COKE":
            case "SPRITE":
            case "WATER":
                logger.info("dispense:send:coldDrinks:{}", order);
                events.coldDrinks().send(MessageBuilder.withPayload(order).build());
                break;
            default:
                logger.info("dispense:send:food:{}", order);
                events.food().send(MessageBuilder.withPayload(order).build());
                break;
        }
    }

    @StreamListener(Events.STAFF)
    public void staffArrivals(String staff) {
        logger.info("staffArrivals:{}", staff);
        if (staff.equals("waitron")) {
            logger.info("staffArrivals:send:open:welcome");
            events.open().send(MessageBuilder.withPayload("welcome").build());
        }
    }

    public interface Events {
        String STAFF = "staff";
        String ORDERS = "orders";
        String FOOD = "food";
        String COLD_DRINKS = "coldDrinks";
        String HOT_DRINKS = "hotDrinks";

        String OPEN = "open";

        @Input(ORDERS)
        SubscribableChannel orders();

        @Input(STAFF)
        SubscribableChannel staff();

        @Output(FOOD)
        MessageChannel food();

        @Output(COLD_DRINKS)
        MessageChannel coldDrinks();

        @Output(HOT_DRINKS)
        MessageChannel hotDrinks();

        @Output(OPEN)
        MessageChannel open();
    }
}
