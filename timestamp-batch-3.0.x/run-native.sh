#!/bin/bash
if [ ! -f ./target/timestamp-batch-task ]; then
  ./mvnw clean native:compile -Pnative
fi
./target/timestamp-batch-task
