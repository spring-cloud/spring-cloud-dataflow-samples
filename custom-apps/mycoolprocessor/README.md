# Custom Spring Cloud Stream Processor
=================

### Creating your Project

Today we're going to talk about how to create a custom Spring Cloud Stream module for use as part of Spring Cloud Data Flow.  We're going to go through all the steps of making a simple processor that will allow you to do convert a Fahrenheit integer temperature to Celsius.  We will be running the demo locally, but all the steps will work in a Cloud Foundry environment as well.  The first step is to create a new spring cloud stream project.  We can do that by going to http://start-scs.cfapps.io/ , which is the spring cloud stream initializer.

![alt text](https://github.com/mross1080/spring-cloud-dataflow-samples/blob/master/custom-apps/mycoolprocessor/assets/screenshot1.png?raw=true "SCS Screen 1")

What you're going to want to do is setup your maven project however you like, I personally used io.mross.MyCoolProcessor.  Then you're going to add your first dependency.  We're making a transform processor here so you're going to want to add the ***transform processor*** as your first dependency to get you started with your project structure.  Secondly you need to choose a message transport binding for your custom app.  You have the option to either choose the ***rabbitmq binder starter*** or ***kafka binder starter***, but I will personally be using RabbitMQ, mainly for ease of deployment in Cloud Foundry.  Once you've set this up your page should look like this.

![alt text](https://github.com/mross1080/spring-cloud-dataflow-samples/blob/master/custom-apps/mycoolprocessor/assets/setupProject.png?raw=true "SCS Screen 2")

Hit the generate project button and then open your new project in an IDE of your choice.

### Developing your module

Now we're at a point where we can actually create our custom module.  In our Spring Cloud Stream application, the minimum configuration is going to require two files.
* MyCoolProcessorAplication.java
* MyCoolProcessorConfiguration.java


Remember, a Spring Cloud Stream is supposed to be a stand alone spring boot application that can be run on it's own.  Spring Cloud Dataflow is the orchestration layer that ties these individual Spring Cloud Stream apps together.

Since I named my project MyCoolProcessor, the Spring Cloud Stream Generator created a MyCoolProcessorApplication.java file.  What's amazing about Spring Cloud Stream is you don't even need to change this file as it is to get off the ground running.  What you do need to do is create a Configuration file where you will define your custom logic.  I am going create a transformer that takes a fahrenheit input and converts it to celcius.  We want to follow the same naming convention as the application file, so create a new file in the same directory called MyCoolProcesorConfiguration.java.


##### MyCoolProcessorConfiguration.java
```
@EnableBinding(Processor.class)
public class MyCoolProcessorConfiguration {

    @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    public int convertToCelsius(String payload) {
        int fahrenheitTemperature = Integer.parseInt(payload);
        return (farenheitTemperature-30)/2;
    }
}
```

If you inspect the file you can see that there are two important spring annotations.  First we annotate the class with **@EnableBinding(Processor.class)**.  Second we create a method and annotate it with ***@Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)***.  By adding these two annotations you are basically classifying this stream module as a Processor(as opposed to a source or a sink).  This allows you to specify that you are receiving input from upstream(Processor.input) and outputting data downstream(Processor.OUTPUT).

In my convertToCelsius method, I am taking the String input, casting it to an integer, and then returning the converted number to celsius.  This method is dead simple, but that is also the beauty of this programming style.  You can add as much logic as you want to this method, and as long as you annotate it properly and return valid output, you now have a proper Spring Cloud Stream processor.

Once you're done putting together the module, move into the origin of your project directory and build a deployable jar with Maven.
```
cd <PATH/TO/MYCOOLPROCESSOR>
mvn clean install
java -jar /target/mycoolprocessor-0.0.1-SNAPSHOT.jar
```

If all goes right you should have a running standalone Spring Boot Application.  Stop the app and then push the Jar to Github to be accessed later.

## Deploying Locally

#### Prerequisites

In order to get started, make sure that you have the following components:

* Local build of https://github.com/spring-cloud/spring-cloud-dataflow [Spring Cloud Data Flow]
* Running instance of RabbitMQ https://www.rabbitmq.com/



 Launch the locally built `server`

```
$ cd <PATH/TO/SPRING-CLOUD-DATAFLOW>
$ java -jar spring-cloud-dataflow-server-local/target/spring-cloud-dataflow-server-local-<VERSION>.jar
```


Connect to Spring Cloud Data Flow's `shell`


```
$ cd <PATH/TO/SPRING-CLOUD-DATAFLOW>
$ java -jar spring-cloud-dataflow-shell/target/spring-cloud-dataflow-shell-<VERSION>.jar

  ____                              ____ _                __
 / ___| _ __  _ __(_)_ __   __ _   / ___| | ___  _   _  __| |
 \___ \| '_ \| '__| | '_ \ / _` | | |   | |/ _ \| | | |/ _` |
  ___) | |_) | |  | | | | | (_| | | |___| | (_) | |_| | (_| |
 |____/| .__/|_|  |_|_| |_|\__, |  \____|_|\___/ \__,_|\__,_|
  ____ |_|    _          __|___/                 __________
 |  _ \  __ _| |_ __ _  |  ___| | _____      __  \ \ \ \ \ \
 | | | |/ _` | __/ _` | | |_  | |/ _ \ \ /\ / /   \ \ \ \ \ \
 | |_| | (_| | || (_| | |  _| | | (_) \ V  V /    / / / / / /
 |____/ \__,_|\__\__,_| |_|   |_|\___/ \_/\_/    /_/_/_/_/_/

<VERSION>

Welcome to the Spring Cloud Data Flow shell. For assistance hit TAB or type "help".
dataflow:>version
<VERSION>
```


[Register](https://github.com/spring-cloud/spring-cloud-dataflow/blob/master/spring-cloud-dataflow-docs/src/main/asciidoc/streams.adoc#register-a-stream-app) RabbitMQ binder variant of out-of-the-box applications
```
dataflow:>app import --uri http://bit.ly/stream-applications-rabbit-maven
```

Register your custom app (I have my custom app jar on this public Github repo for ease of deployment)

```
app register --type processor --name convertToCelsius --uri https://github.com/mross1080/spring-cloud-dataflow-samples/raw/master/custom-apps/mycoolprocessor/assets/mycoolprocessor-0.0.1-SNAPSHOT.jar --force
```

Create the stream

```
dataflow:>stream create --name convertToCelsiusStream --definition "http  --port=9090 | convertToCelsius | log" --deploy --deploy

Created and deployed new stream 'convertToCelsiusStream'
```


Verify the stream is successfully deployed

```
dataflow:>stream list
```


Verify that the apps have successfully deployed

```
dataflow:>runtime apps
```

 Notice that `convertToCelsiusStream.http`,`convertToCelsiusStream.log` and `convertToCelsiusStream.convertToCelsius` https://github.com/spring-cloud/spring-cloud-stream-modules/ [Spring Cloud Stream] modules are running as Spring Boot applications within the Local `server` as collocated processes.  Also since we are running locally, note the file location of the logs.  


```
2016-09-27 10:03:11.988  INFO 95234 --- [nio-9393-exec-9] o.s.c.d.spi.local.LocalAppDeployer       : deploying app convertToCelsiusStream.log instance 0
   Logs will be in /var/folders/2q/krqwcbhj2d58csmthyq_n1nw0000gp/T/spring-cloud-dataflow-3236898888473815319/convertToCelsiusStream-1474984991968/convertToCelsiusStream.log
2016-09-27 10:03:12.397  INFO 95234 --- [nio-9393-exec-9] o.s.c.d.spi.local.LocalAppDeployer       : deploying app convertToCelsiusStream.convertToCelsius instance 0
   Logs will be in /var/folders/2q/krqwcbhj2d58csmthyq_n1nw0000gp/T/spring-cloud-dataflow-3236898888473815319/convertToCelsiusStream-1474984992392/convertToCelsiusStream.convertToCelsius
2016-09-27 10:03:14.445  INFO 95234 --- [nio-9393-exec-9] o.s.c.d.spi.local.LocalAppDeployer       : deploying app convertToCelsiusStream.http instance 0
   Logs will be in /var/folders/2q/krqwcbhj2d58csmthyq_n1nw0000gp/T/spring-cloud-dataflow-3236898888473815319/convertToCelsiusStream-1474984994440/convertToCelsiusStream.http
```

 Post sample data pointing to the `http` endpoint: `http://localhost:9090` [`9090` is the `server.port` we specified for the `http` source in this case]


```
dataflow:>http post --target http://localhost:9090 --data 76
> POST (text/plain;Charset=UTF-8) http://localhost:9090 76
> 202 ACCEPTED
```

Open the logs for the stream you created to see the output of our stream

```
2016-09-27 10:05:34.933  INFO 95616 --- [CelsiusStream-1] log.sink                                 : 23
```

 That's it; you're done!
