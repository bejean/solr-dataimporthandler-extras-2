#!/bin/bash
export COPY_EXTENDED_ATTRIBUTES_DISABLE=true
export COPYFILE_DISABLE=true

mvn clean
mvn package -Dmaven.test.skip=true

find . -name .DS_Store -type f -exec rm {} \;

rm dih-jdbc-datasource.tar
tar cfz dih-jdbc-datasource.tar README.md build.sh pom.xml src target/*.jar