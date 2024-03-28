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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Corneil du Plessis
 */

@Configuration
public class Kitchen {
    private static final Logger logger = LoggerFactory.getLogger(Kitchen.class);
    private final StreamBridge bridge;

    public Kitchen(StreamBridge bridge) {

        this.bridge = bridge;
    }

    @Bean(name = Events.ORDERS)
    public Consumer<String> acceptOrders() {
        return (String order) -> {
            logger.info("acceptOrder:{}", order);
            switch (order.toUpperCase()) {
                case "COFFEE":
                case "TEA":
                    logger.info("dispense:send:hotDrinks:{}", order);
                    bridge.send(Events.HOT_DRINKS, MessageBuilder.withPayload(order).build());
                    break;
                case "COKE":
                case "SPRITE":
                case "WATER":
                    logger.info("dispense:send:coldDrinks:{}", order);
                    bridge.send(Events.COLD_DRINKS, MessageBuilder.withPayload(order).build());
                    break;
                default:
                    logger.info("dispense:send:food:{}", order);
                    bridge.send(Events.FOOD, MessageBuilder.withPayload(order).build());
                    break;
            }
        };
    }

    @Bean(name = Events.STAFF)
    public Consumer<String> staffArrivals() {
        return (String staff) -> {
            logger.info("staffArrivals:{}", staff);
            if (staff.equals("waitron")) {
                logger.info("staffArrivals:send:open:welcome");
                bridge.send(Events.OPEN, MessageBuilder.withPayload("welcome").build());
            }
        };
    }

    public interface Events {
        String STAFF = "staff";
        String ORDERS = "orders";
        String FOOD = "food";
        String COLD_DRINKS = "coldDrinks";
        String HOT_DRINKS = "hotDrinks";
        String OPEN = "open";

    }
}
