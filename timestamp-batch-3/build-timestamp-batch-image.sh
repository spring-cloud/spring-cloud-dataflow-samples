#!/bin/bash
if [ "$TIMESTAMP_BATCH_TASK_VERSION" = "" ]; then
  TIMESTAMP_BATCH_TASK_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
fi
./mvnw -o clean install -DskipTests
./mvnw -o spring-boot:build-image -DskipTests -Dspring-boot.build-image.imageName=springcloud/timestamp-batch-task:$TIMESTAMP_BATCH_TASK_VERSION
