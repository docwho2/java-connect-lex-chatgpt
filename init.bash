#!/bin/bash

# ensure sub modules are brought in
git submodule update --init

# Build Up serialization
mvn install -f LambdaEventsV4/aws-lambda-java-serialization/pom.xml -DskipTests

# Build Up Java Events V4
mvn install -f LambdaEventsV4/aws-lambda-java-events/pom.xml -DskipTests

# and finally parent pom for the lambdas
mvn install
