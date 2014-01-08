#!/bin/bash

repo=$1
repo_objc=$2
branch=$3
tag=$4
message=$5

cd $repo
return_val=$?
if [ "$return_val" != "0" ]; then
  exit $return_val
fi
if [ "$branch" == "" ]; then
    echo "$repo" "$repo_objc" "$tag" "[tag]" "$message"
else
    echo "$repo" "$repo_objc" "$branch" "[branch]" "$message"
fi