#!/bin/bash
set +e
docker inspect springcloudtask/timestamp-batch-task:3.0.0 > /dev/null
RC=$?
set -e
if((RC>0)); then
  ./mvnw clean spring-boot:build-image -Pnative
fi
docker run springcloudtask/timestamp-batch-task:3.0.0
