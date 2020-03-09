#!/usr/bin/env bash

set -euf

AZUL_GPG_KEY=0xB1998361219BD9C9
ZULU_VERSION=11
ZULU_RELEASE=11.37+17
JAVA_HOME=/usr/lib/jvm/zulu-11-amd64

sudo apt-get update
sudo apt-get install -y gnupg2 locales
sudo locale-gen en_US.UTF-8
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys ${AZUL_GPG_KEY}
echo "deb http://repos.azulsystems.com/ubuntu stable main" | sudo tee -a /etc/apt/sources.list.d/zulu.list
sudo apt-get update
sudo apt-get install -y zulu-${ZULU_VERSION}=${ZULU_RELEASE}
export ALTERNATIVES_JAVA=$(realpath /etc/alternatives/java)
export JAVA_HOME=${ALTERNATIVES_JAVA%/bin/java}
export PATH=$JAVA_HOME/bin:$PATH
java -version

set +euf