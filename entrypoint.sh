#!/bin/bash

set -e

# Give the arguments nice variable names
SOURCE_REPO=$1
TARGET_REPO=$2
START_DATE=$3
ASSIGNEE=$4
GITHUB_USERNAME=$5
GITHUB_TOKEN=$6

# Create tmp file with GitHub credentials
echo "login=$GITHUB_USERNAME" > /github.properties
echo "password=$GITHUB_TOKEN" > /github.properties

# Determin JAR file name
JAR=release-to-issue-java-*-jar-with-dependencies.jar

# Run Java program
java -jar $JAR --sourcerepo $SOURCE_REPO --targetrepo $TARGET_REPO --datelimit $START_DATE --assignee $ASSIGNEE --dryrun

# Delete tmp file with GitHub credentials
rm -f /github.properties
exit 0
