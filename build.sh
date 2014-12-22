#!/bin/bash
export COPY_EXTENDED_ATTRIBUTES_DISABLE=true
export COPYFILE_DISABLE=true

mvn clean
mvn package -Dmaven.test.skip=true

find . -name .DS_Store -type f -exec rm {} \;

tar cfz dih-jdbc-datasource.tar build.sh pom.xml src target/*.jar