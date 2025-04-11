#!/bin/bash

set -e

# Define the curl binary path and parameters
CURL="/usr/bin/curl -f -S -s"

# Check arguments
if [[ -z "$1" ]]; then
	echo "=> No parameter(s) given. Exit."; exit 1;
fi

if [[ -z "$2" ]]; then
	echo "=> Second parameter is missing. Exit."; exit 1;
fi

# Give the arguments nice variable names
REPO=$1
START_DATE=$2
TARGET_REPO="maxkratz/docker_texlive"

# Get all releases from the GitHub API
get_releases () {
    release_json=$($CURL -L \
        -H "Accept: application/vnd.github+json" \
        -H "X-GitHub-Api-Version: 2022-11-28" \
        https://api.github.com/repos/$REPO/releases)
    # echo $release_json
    names=$(echo $release_json | jq -r .[].name)
    publish_dates=$(echo $release_json | jq -r .[].published_at)

    IFS_ORIG=$IFS  # Save current IFS (Internal Field Separator)
    IFS=$'\n'      # Change IFS to newline char
    names=($names) # split the `names` string into an array by the same name
    publish_dates=($publish_dates)
    IFS=$IFS_ORIG  # Restore original IFS
}

# Convert a given date string to an epoch
date_string_to_epoch () {
    date -d $1 +"%s"
}

# Set the start epoch value to the provided date value
set_start_epoch () {
    start_epoch=$(date_string_to_epoch $START_DATE)
}

# Get all issues of the target GitHub repository
get_issues () {
  # issues_json=$($CURL -L \
  #   -H "Accept: application/vnd.github+json" \
  #   -H "X-GitHub-Api-Version: 2022-11-28" \
  #   https://api.github.com/repos/$TARGET_REPO/issues?state=all\&page=0)

  # TODO: ^while loop (with an upper limit ...) that exits when a request does not return any issues.

  declare -a titles=(  )

  i=0
  for limit in $(seq 1 33334);
  do
      issues_i_json=$($CURL -L \
        -H "Accept: application/vnd.github+json" \
        -H "X-GitHub-Api-Version: 2022-11-28" \
        https://api.github.com/repos/$TARGET_REPO/issues\?state\=all\&page\=$i)

      if [ -z "${issues_i_json}" ]; then
        break;
      fi

      # echo $issue_i_json
      titles_i=$(echo $issue_i_json | jq -r .[].title)

      # echo $titles_i

      IFS_ORIG=$IFS  # Save current IFS (Internal Field Separator)
      IFS=$'\n'      # Change IFS to newline char
      titles_i=($titles_i) # split the `titles` string into an array by the same name
      IFS=$IFS_ORIG  # Restore original IFS

      echo ${titles_i[@]}

      titles+=(${titles_i[@]})
      i+=1
  done
}

get_issues

for (( i=0; i<${#titles[@]}; i++ ));
do
  echo "index: $i, title: ${titles[$i]}"
done

exit 0

#
# Script
#

get_releases
set_start_epoch

# For all found releases ...
for (( i=0; i<${#publish_dates[@]}; i++ ));
do
  publish_epoch=$(date_string_to_epoch ${publish_dates[$i]})

  # ... check if a particular release was after the start date
  if [ "$start_epoch" -lt "$publish_epoch" ]; then
    echo "index: $i, value: $publish_epoch"
  fi
done

# TODO:
# - get all issues of the target GitHub repository
# - check each matched release from above -> is there already an issue?
# - for all other releases -> create a new issue per release

# TODO
exit 0