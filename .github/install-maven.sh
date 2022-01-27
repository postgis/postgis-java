#!/usr/bin/env bash

set -euf

MAVEN_BASE_URL=https://archive.apache.org/dist/maven/maven-3/
MAVEN_VERSION=3.8.4
MAVEN_SHA=2cdc9c519427bb20fdc25bef5a9063b790e4abd930e7b14b4e9f4863d6f9f13c

sudo apt-get update
sudo apt-get install -y curl
sudo mkdir -p /usr/share/maven /usr/share/maven/ref
sudo curl -fsSL -o /tmp/apache-maven.tar.gz ${MAVEN_BASE_URL}/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz
echo "${MAVEN_SHA} /tmp/apache-maven.tar.gz" | sha256sum -c -
sudo tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1
sudo rm -f /tmp/apache-maven.tar.gz
sudo ln -fs /usr/share/maven/bin/mvn /usr/bin/mvn
mvn -version

set +euf

