#!/bin/bash
workdir=$(cd $(dirname $0); pwd)
cd workdir/bsf-dependencies && mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$1 && mvn deploy -Dmaven.test.skip=true -P release
cd workdir/bsf-stater && mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$1 && mvn deploy -Dmaven.test.skip=true -P release
cd workdir && mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$1 && mvn deploy -Dmaven.test.skip=true -P release