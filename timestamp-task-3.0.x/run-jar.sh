#!/bin/bash
if [ ! -f ./target/timestamp-task-3.0.0.jar ]; then
  ./mvnw clean package
fi
java -jar ./target/timestamp-task-3.0.0.jar