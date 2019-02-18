#!/usr/bin/env bash

root_dir="`dirname $0`/../.."
cd ${root_dir}

mkdir -p .idea/
mkdir -p .idea/codeStyles/
mkdir -p .idea/inspectionProfiles/
cp config/intellij/gradle-template.xml .idea/gradle.xml

./gradlew cleanIdea
./gradlew idea

rm crawler4j-parent.ipr crawler4j-parent.iml crawler4j-parent.iws
