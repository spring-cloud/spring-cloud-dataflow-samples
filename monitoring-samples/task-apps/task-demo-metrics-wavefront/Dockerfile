FROM springcloud/baseimage:1.0.0
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
VOLUME ["/tmp"]
ENTRYPOINT ["java","-jar","/app.jar"]
