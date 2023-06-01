#!/bin/bash
if [ "$TIMESTAMP_TASK_VERSION" = "" ]; then
  TIMESTAMP_TASK_VERSION=2.0.2
fi
./mvnw -o clean install -DskipTests
./mvnw -o spring-boot:build-image -DskipTests -Dspring-boot.build-image.imageName=springcloud/timestamp-task:$TIMESTAMP_TASK_VERSION
