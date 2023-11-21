#!/bin/bash

# ensure sub modules are brought in
git submodule update --init

# and finally parent pom for the lambdas
mvn install -DskipTests
