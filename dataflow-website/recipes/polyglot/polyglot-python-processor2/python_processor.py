#!/usr/bin/env python

import os
import sys

from kafka import KafkaConsumer, KafkaProducer
from util.http_status_server import HttpHealthServer
from util.task_args import get_kafka_binder_brokers, get_input_channel, get_output_channel, get_reverse_string

consumer = KafkaConsumer(get_input_channel(), bootstrap_servers=[get_kafka_binder_brokers()])
producer = KafkaProducer(bootstrap_servers=[get_kafka_binder_brokers()])

HttpHealthServer.run_thread()

while True:
    for message in consumer:
        output_message = message.value
        reverse_string = get_reverse_string()

        if reverse_string is not None and reverse_string.lower() == "true":
            output_message = "".join(reversed(message.value))

        producer.send(get_output_channel(), output_message)

