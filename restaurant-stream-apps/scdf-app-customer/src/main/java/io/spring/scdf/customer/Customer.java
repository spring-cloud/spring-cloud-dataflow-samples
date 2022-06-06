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
package io.spring.scdf.customer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
@Component
@EnableBinding(Customer.Events.class)
public class Customer {
    private static final Logger logger = LoggerFactory.getLogger(Customer.class);
    private Random random = new Random(System.currentTimeMillis());
    private List<String> cold = Arrays.asList("water", "coke", "sprite");
    private List<String> hot = Arrays.asList("coffee", "tea");
    private List<String> food = Arrays.asList("burger", "pizza", "steak", "pasta");
    private final Events events;
    private boolean placedOrder = false;

    public Customer(Events events) {
        this.events = events;
    }

    public void placeOrders() {
        if (!placedOrder) {
            logger.info("placeOrder:start");
            placeOrder(cold);
            placeOrder(food);
            placeOrder(hot);
            placedOrder = true;
            logger.info("placeOrder:end");
        } else {
            logger.info("placeOrder:done");
        }
    }

    public void placeOrder(List<String> items) {
        String item = items.get(random.nextInt(items.size()));
        logger.info("placeOrder:send:order:{}", item);
        events.order().send(MessageBuilder.withPayload(item).build());
    }


    @StreamListener(Events.RECEIVE)
    public void receive(String order) {
        String message = "money for " + order;
        logger.info("receive:{}:send:payment:{}", order, message);

        events.payment().send(MessageBuilder.withPayload(message).build());
    }

    @StreamListener(Events.OPEN)
    public void isOpen(String message) {
        logger.info("isOpen:{}", message);
        placeOrders();
    }

    public interface Events {
        String OPEN = "open";
        String ORDER = "order";
        String RECEIVE = "receive";
        String PAYMENT = "payment";

        @Output(ORDER)
        MessageChannel order();

        @Output(PAYMENT)
        MessageChannel payment();

        @Input(RECEIVE)
        SubscribableChannel receive();

        @Input(OPEN)
        SubscribableChannel open();
    }
}
