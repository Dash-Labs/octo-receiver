#!/bin/bash

repo=$1
repo_objc=$2
repo_name=$3
branch=$4
tag=$5
message=$6
author=$7

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