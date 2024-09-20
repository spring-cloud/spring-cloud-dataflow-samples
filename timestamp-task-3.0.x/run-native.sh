#!/bin/bash
if [ ! -f ./target/timestamp-task ]; then
  ./mvnw clean native:compile -Pnative
fi
./target/timestamp-task
