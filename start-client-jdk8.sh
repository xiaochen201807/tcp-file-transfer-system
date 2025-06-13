#!/bin/bash

echo "Starting TCP Client with JDK 1.8..."
echo "Checking Java version..."
java -version
echo

# 设置JDK 1.8路径（根据实际情况修改）
# export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
# export PATH=$JAVA_HOME/bin:$PATH

cd tcp-client

# 设置Maven内存参数
export MAVEN_OPTS="-Xmx512m -Xms256m"

echo "Starting client..."
mvn spring-boot:run
