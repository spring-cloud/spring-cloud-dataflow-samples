
# Auto-scaling adapter for Streaming Data Pipelines

The `AlertWebHookApplication` is a [Prometheus Alertmanager Webhook Receiver](https://github.com/prometheus/alertmanager) 
that listens for pre-configured Prometheus alerts and leverages the SCDF Scale API to scale out or in a preconfigured 
stream application.

Use the `scdf.alert.webhook.scaleOutAlertName` and  `scdf.alert.webhook.scaleInAlertName` to configure the names of the scale-out and scale-in alert names.

On scale-out alert, the `AlertWebHookApplication` increases the number of application (defined by `scdf.alert.webhook.scaleApplicationName` or `application_name` alert label) instances  to the `scdf.alert.webhook.scaleOutFactor` count.

On scale-in alert, the `AlertWebHookApplication` decreases the number of application (defined by `scdf.alert.webhook.scaleApplicationName`  or `application_name` alert label) instances to the `scdf.alert.webhook.scaleInFactor` count.

The stream name should be provided either as `scdf.alert.webhook.scaleStreamName` property or an Alert label called: `stream_name`.

The application name should be provided either as `scdf.alert.webhook.scaleApplicationName` property or an Alert label called: `application_name`.

Internally, `AlertWebHookApplication` uses the Data Flow Scale REST API to scale the app instances in platform agnostic way.

Use the `spring.cloud.dataflow.client.server-uri` property to configure url of the Data Flow server.

