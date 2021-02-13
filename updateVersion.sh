#!/bin/bash
cd bsf-dependencies
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$1 && mvn deploy -Dmaven.test.skip=true -P release
cd ../bsf-stater
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$1 && mvn deploy -Dmaven.test.skip=true -P release
cd ..
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$1 && mvn deploy -Dmaven.test.skip=true -P release