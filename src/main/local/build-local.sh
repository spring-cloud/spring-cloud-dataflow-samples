#!/usr/bin/env bash
pushd  'timestamp-task'
  ./mvnw -B clean install spring-boot:build-image -Dspring-boot.build-image.imageName=springcloudtask/timestamp-task:2.0.2
popd
pushd 'timestamp-batch'
  ./mvnw -B clean install spring-boot:build-image -Dspring-boot.build-image.imageName=springcloudtask/timestamp-batch-task:2.0.2
popd

pushd 'restaurant-stream-apps/scdf-app-customer'
  ./mvnw -B clean install spring-boot:build-image -Dspring-boot.build-image.imageName=springcloudstream/scdf-app-customer:1.0.0-SNAPSHOT
popd

pushd 'restaurant-stream-apps/scdf-app-kitchen'
  ./mvnw -B clean install spring-boot:build-image -Dspring-boot.build-image.imageName=springcloudstream/scdf-app-kitchen:1.0.0-SNAPSHOT
popd

pushd 'restaurant-stream-apps/scdf-app-waitron'
  ./mvnw -B clean install spring-boot:build-image -Dspring-boot.build-image.imageName=springcloudstream/scdf-app-waitron:1.0.0-SNAPSHOT
popd