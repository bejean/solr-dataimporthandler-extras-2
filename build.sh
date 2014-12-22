#!/bin/bash
export COPY_EXTENDED_ATTRIBUTES_DISABLE=true
export COPYFILE_DISABLE=true

mvn clean
mvn package -Dmaven.test.skip=true