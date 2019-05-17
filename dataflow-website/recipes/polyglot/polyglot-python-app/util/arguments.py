import os
import sys
from collections import defaultdict


def get_cmd_arg(name):
    """Extracts argument value by name. (@author: Chris Schaefer)
  
    Assumes the exec (default) spring-cloud-deployer-k8s argument passing mode.
  
    Args:
      name: argument name.
    Returns:
      value of the requested argument.
    """
    d = defaultdict(list)
    for k, v in ((k.lstrip('-'), v) for k, v in (a.split('=') for a in sys.argv[1:])):
        d[k].append(v)

    if bool(d[name]):
        return d[name][0]
    else:
        return ''


def get_stream_app_label():
    return get_cmd_arg('spring.cloud.dataflow.stream.app.label')


def get_stream_name():
    return get_cmd_arg('spring.cloud.dataflow.stream.name')


def get_channel_topic(channel_name):
    """
    For given channel name returns the message broker destinations (e.g. Kafka topics or RabbitMQ exchanges).

    We adopt the Spring Cloud Stream using the following format:
                   spring.cloud.stream.bindings.<channelName>.destination=<value>.
    The <channelName> represents the name of the channel being configured (for example, input or output).

    :param channel_name: logical channel name as defined in the application.
    :return: The target destination of a channel on the bound middleware (for example, the RabbitMQ exchange or Kafka
             topic). If the channel is bound as a consumer, it could be bound to multiple destinations, and the
             destination names can be specified as comma-separated String values.
    """
    return get_cmd_arg('spring.cloud.stream.bindings.{}.destination'.format(channel_name))


def get_kafka_brokers():
    return os.getenv('SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS', '')


def get_kafka_zk_nodes():
    return os.getenv('SPRING_CLOUD_STREAM_KAFKA_BINDER_ZK_NODES', '')


def get_application_guid():
    return os.getenv('SPRING_CLOUD_APPLICATION_GUID', '')


def get_application_group():
    return os.getenv('SPRING_CLOUD_APPLICATION_GROUP', '')


def get_env_info():
    props = '  stream-name={}\n  app-name={}\n  app-guid={}\n  app-group={}\n  kafka-brokers={}\n  ' \
            'kafka-zk={}\n'.format(get_stream_name(), get_stream_app_label(), get_application_guid(),
                                   get_application_group(), get_kafka_brokers(), get_kafka_zk_nodes())
    channels = '  Inputs:\n    input={}\n  Outputs: \n    even={}\n    odd={}\n'.format(
        get_channel_topic('input'), get_channel_topic('even'), get_channel_topic('odd'))
    args = '\n   '.join(sys.argv)
    envs = ''
    # envs = '\n  '.join(list(map(lambda k: '{}={}'.format(k, os.environ[k]), os.environ)))
    return 'Properties\n{0}\nChannels\n{1}\nArguments\n  {2}\n\nEnvironment\n  {3}'.format(
        props, channels, args, envs)
