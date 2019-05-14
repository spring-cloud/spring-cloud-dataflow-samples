from kafka import KafkaConsumer, KafkaProducer
from kafka.admin import KafkaAdminClient, NewTopic
from kafka.errors import TopicAlreadyExistsError

from util.actuator import Actuator
from util.arguments import get_kafka_brokers, get_env_info, get_channel_topic


class Barista:
    """Barista continuously process input drink orders and servers hot drinks for the even order numbers and cold drink
    for the ood orders.
    The orders come through the `orders` input channel. The hot and cold drinks are served through either the
    `hot.drink` or the `cold.drink` output channels.

     :param info: Information about the app configuration.
     :param kafka_brokers: Kafka brokers connection uri.
     :param orders: Orders topic name.
     :param hot_drinks: Hot drinks topic name.
     :param cold_drinks: Cold drinks topic name.
     """

    def __init__(self, info, kafka_brokers, orders, hot_drinks, cold_drinks):

        self.kafka_brokers = kafka_brokers
        self.orders_topic = orders
        self.hot_drink_topic = hot_drinks
        self.cold_drink_topic = cold_drinks

        # Serve the liveliness and readiness probes via http server in a separate thread.
        Actuator.start(port=8080, info=info)

        # Ensure the output topics exist.
        self.__create_topics_if_missing([self.orders_topic, self.hot_drink_topic, self.cold_drink_topic])

        self.consumer = KafkaConsumer(self.orders_topic, bootstrap_servers=self.kafka_brokers)
        self.producer = KafkaProducer(bootstrap_servers=self.kafka_brokers)

    def __create_topics_if_missing(self, topic_names):
        admin_client = KafkaAdminClient(bootstrap_servers=self.kafka_brokers, client_id='test')
        for topic in topic_names:
            try:
                new_topic = NewTopic(name=topic, num_partitions=1, replication_factor=1)
                admin_client.create_topics(new_topics=[new_topic], validate_only=False)
            except TopicAlreadyExistsError:
                print ('Topic: {} already exists!')

    def process_orders(self):
        """
        Continuously consumes orders form the input channel and send hot or cold drinks to the output channels.
        The even order number stands for a hot drink request while an odd number stands for a cold drink request.
        """
        while True:
            for message in self.consumer:
                if message.value is not None:
                    if self.is_even_order(message.value):
                        self.producer.send(self.hot_drink_topic, b'Serve Hot drink for order:' + message.value)
                    else:
                        self.producer.send(self.cold_drink_topic, b'Serve Cold drink for order:' + message.value)

    @staticmethod
    def is_even_order(value):
        return int(value[-1:]) % 2 == 0


Barista(
    get_env_info(),
    get_kafka_brokers(),
    get_channel_topic('orders'),
    get_channel_topic('hot.drink'),
    get_channel_topic('cold.drink')
).process_orders()
