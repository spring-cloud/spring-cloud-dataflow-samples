FROM java:8-alpine

ARG JAR_FILE

ADD target/${JAR_FILE} taskapp-2.0.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/taskapp-2.0.0-SNAPSHOT.jar"]