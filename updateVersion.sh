#!/bin/bash
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$0
mvn deploy -Dmaven.test.skip=true -P release

