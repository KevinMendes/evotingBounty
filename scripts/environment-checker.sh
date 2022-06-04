#!/bin/bash
#
# Script for the environment check.
# The script checks for tools installation and versions.
#

log() {
  echo "$(date) [$$] INFO $*"
}

warn() {
  echo "$(date) [$$] WARN $*"
}

die() {
  echo "$(date) [$$] ERROR $*"
  exit $1
}

check_version() {
  echo "$1" | egrep -q "$2" || die $3 "$4"
}

log 'Checking tools and versions ...'

check_version "$(java -version 2>&1)" 'version "1.8.0_312' 2 'java version'
check_version "$(mvn -v)" 'Apache Maven 3.8.5' 3 'maven version'
check_version "$(node -v)" 'v14.17.5' 3 'node version'
check_version "$(npm -v)" '6.14.14' 3 'npm version'
check_version "$(grunt -v)" 'v1.3.2' 3 'grunt version'
check_version "$(gulp -v)" 'CLI version: 2.3.0' 3 'gulp version'
check_version "$(phantomjs --version)" '2.1.1' 3 'phantomjs version'

log "All tools and versions correct."

log 'Checking for environment variables ...'

if [[ -z "${EVOTING_HOME}" ]]; then
  die 1 'EVOTING_HOME environment variable is undefined'
fi

if [[ -z "${DOCKER_REGISTRY}" ]]; then
  warn "DOCKER_REGISTRY is undefined, it will be needed for a correct E2E test"
fi

if [[ -z "${EVOTING_DOCKER_HOME}" ]]; then
  warn "EVOTING_DOCKER_HOME is undefined, it will be needed for a correct E2E test"
fi

log 'All environment variables correctly set.'
