#!/bin/bash

set -e

# Define the curl binary path and parameters
# CURL="/usr/bin/curl -f -S -s"
CURL="curl -f -S -s"

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
    # release_json=$($CURL -L \
    #     -H "Accept: application/vnd.github+json" \
    #     -H "X-GitHub-Api-Version: 2022-11-28" \
    #     https://api.github.com/repos/$REPO/releases)

    release_json=$(cat release.json)

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

  # ids=(  )
  titles=(  )

  i=0
  # for limit in $(seq 1 33334);
  for limit in $(seq 1 1);
  # TODO: fix limit i.e. curl error code 422
  do
      # issues_i_json=$($CURL -L \
      #   -H "Accept: application/vnd.github+json" \
      #   -H "X-GitHub-Api-Version: 2022-11-28" \
      #   "https://api.github.com/repos/$TARGET_REPO/issues?state=all&page=$i")
      issues_i_json=$(cat test.json)

      #echo $TARGET_REPO
      #echo $issues_i_json

      if [ "$?" -gt 0 ]; then
        break;
      fi

      if [ -z "${issues_i_json}" ]; then
        break;
      fi

      # ids_i=$(echo $issues_i_json | jq -r .[].id)
      titles_i=$(echo $issues_i_json | jq -r .[].title)
      # echo "$ids_i"
      # echo "$titles_i"

      # echo $issues_i_json | jq -r .[].title


      IFS_ORIG=$IFS  # Save current IFS (Internal Field Separator)
      IFS=$'\n'      # Change IFS to newline char
      titles_i=($titles_i) # split the `names` string into an array by the same name
      IFS=$IFS_ORIG  # Restore original IFS


      # ids+=(${ids_i[@]})
      titles+=("${titles_i[@]}")
      # for (( j=0; j<${#titles_i[@]}; j++ ));
      # do
      #   titles+=("${titles_i[$j]}")
      # done
      # ids+=($(echo $issues_i_json | jq -r .[].title))
      i+=1
  done
}

# get_issues

# # for (( i=0; i<${#ids[@]}; i++ ));
# # do
# #   echo "index: $i, id: ${ids[$i]}"
# # done

# for (( i=0; i<${#titles[@]}; i++ ));
# do
#   echo "index: $i, title: ${titles[$i]}"
# done

# exit 0

#
# Script
#

get_releases
get_issues
set_start_epoch

# For all found releases ...
relevant_release_ids=(  )
for (( i=0; i<${#publish_dates[@]}; i++ ));
do
  publish_epoch=$(date_string_to_epoch ${publish_dates[$i]})

  # ... check if a particular release was after the start date
  if [ "$start_epoch" -lt "$publish_epoch" ]; then
    # echo "index: $i, value: $publish_epoch"
    relevant_release_ids+=($i)
  fi
done

# TODO:
# - get all issues of the target GitHub repository
# - check each matched release from above -> is there already an issue?
# - for all other releases -> create a new issue per release

for (( i=0; i<${#relevant_release_ids[@]}; i++ ));
do
  echo "index: $i, title: ${relevant_release_ids[$i]}"
done

# TODO
exit 0