#!/usr/bin/env bash

docker pull mockserver/mockserver:maven
docker run -v $(pwd):/build/mockserver-maven-plugin -w /build/mockserver-maven-plugin -a stdout -a stderr -e BUILDKITE_BRANCH=$BUILDKITE_BRANCH mockserver/mockserver:maven /build/mockserver-maven-plugin/scripts/buildkite_quick_build.sh
