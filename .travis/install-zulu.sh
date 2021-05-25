#!/usr/bin/env bash

set -euf

AZUL_GPG_KEY=0xB1998361219BD9C9
ZULU_VERSION=8
ZULU_RELEASE=8.0.292-1
JAVA_HOME=/usr/lib/jvm/zulu-8-amd64

sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys ${AZUL_GPG_KEY}
sudo apt-add-repository 'deb http://repos.azulsystems.com/ubuntu stable main'
sudo apt-get update
sudo apt-get install -y zulu-repo
sudo apt-get update
sudo apt-get install -y zulu${ZULU_VERSION}=${ZULU_RELEASE}
sudo sed -i.orig -e "s/^hl /jre /g" -e "s/^jdkhl /jdk /g" /usr/lib/jvm/.zulu8-ca-amd64.jinfo
sudo update-java-alternatives --set zulu8-ca-amd64
export ALTERNATIVES_JAVAC=$(realpath /etc/alternatives/javac)
export JAVA_HOME=${ALTERNATIVES_JAVAC%/bin/javac}
export PATH=$JAVA_HOME/bin:$PATH
java -version

set +euf
