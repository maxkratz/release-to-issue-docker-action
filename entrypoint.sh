#!/bin/bash

set -e

CURL="/usr/bin/curl -f -S -s"

# Check arguments
if [[ -z "$1" ]]; then
	echo "=> No parameter(s) given. Exit."; exit 1 ;
fi

# if [[ -z "$2" ]]; then
# 	echo "=> Second parameter is missing. Exit."; exit 1 ;
# fi

# Give the arguments nice variable names
REPO=$1
# START_DATE=$2

get_releases () {
    release_json=$($CURL -L \
        -H "Accept: application/vnd.github+json" \
        -H "X-GitHub-Api-Version: 2022-11-28" \
        https://api.github.com/repos/$REPO/releases)
    # echo $release_json
    names=$(echo $release_json | jq -r .[].name)
    publish_dates=$(echo $release_json | jq -r .[].published_at)

    SAVEIFS=$IFS   # Save current IFS (Internal Field Separator)
    IFS=$'\n'      # Change IFS to newline char
    names=($names) # split the `names` string into an array by the same name
    publish_dates=($publish_dates)
    IFS=$SAVEIFS   # Restore original IFS
}

date_string_to_epoch () {
    date -d $1 +"%s"
}



get_releases

# TODO

for (( i=0; i<${#names[@]}; i++ ));
do
  echo "index: $i, value: ${names[$i]}"
done

for (( i=0; i<${#publish_dates[@]}; i++ ));
do
  echo "index: $i, value: $(date_string_to_epoch ${publish_dates[$i]})"
done
