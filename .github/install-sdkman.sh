#!/usr/bin/env bash

set -euf -o pipefail

JAVA_VERSION=$1
MAVEN_VERSION=$2

printf "Installing, configuring, and initializing SDKMAN\n"
curl -s "https://get.sdkman.io" | bash
sed -i -e "s/sdkman_auto_answer=false/sdkman_auto_answer=true/" "${HOME}/.sdkman/etc/config"
set +uf
source "${HOME}/.sdkman/bin/sdkman-init.sh"

printf "Installing Azul Zulu JDKs via SDKMAN\n"
sdk i java ${JAVA_VERSION}

printf "Installing Apache Maven via SDKMAN\n"
sdk i maven ${MAVEN_VERSION}
