#!/bin/bash

STATUS=0
NEWLINE=$'\n'
subspec_template=""

repo=$1
repo_cocoa=$2
branch=$3
tag=$4
message=$5
author=$6

checkStatus () {
 if [ "$STATUS" != "0" ]; then
  echo "**Error: $1"
  exit $STATUS
 fi
}

iterateSubspecs() {
  local indent=$1
  local spec=$2
  local directory=$3
  for f in *; do
    if [[ -d $f ]]; then
      local fileCount=`find $f/ -maxdepth 1 -type f -name '*.*' | wc -l | sed 's/^ *//g'`
      local specIncr=""
      local indentIncr=""
      if [[ "$fileCount" != "0" ]]; then
        specIncr="s"
        indentIncr="  "
        subspec_template="$subspec_template$indent${spec}s.subspec '$f' do |${spec}ss|$NEWLINE"
        subspec_template="$subspec_template$indent  ${spec}ss.source_files = 'src/$directory$f/*.{h,m}'$NEWLINE"
        subspec_template="$subspec_template$indent  ${spec}ss.header_dir = '$directory$f'$NEWLINE"
      fi
      cd $f
      iterateSubspecs "$indent$indentIncr" "$spec$specIncr" "$directory$f/"
      cd ..
      if [[ "$fileCount" != "0" ]]; then
        subspec_template="$subspec_template${indent}end$NEWLINE"
      fi
    fi
  done
}

repo_name="${repo##*/}"
echo "Looking for cocoapod spec changes for repository $repo_name"

if [ "$branch" != "" ]; then
  echo "Non-tag push, ignoring cocoapod spec changes for repository $repo_name"
  exit $STATUS
fi

cd $repo_cocoa
STATUS=$?
checkStatus "Could not change into repository dir $repo_cocoa"

git pull
STATUS=$?
checkStatus "Could not git pull within $repo_cocoa"

template=$(<template.podspec)

if [[ -z "$template" ]]; then
  echo "Empty or non-existance 'template.podspec' in $repo_cocoa"
  exit 1;
fi

if [[ "$template" == *"%%SUBSPEC%%"* ]]; then
  # need to do subspec
  echo "Creating subspecs within $repo_cocoa"
  pushd $repo
  STATUS=$?
  checkStatus "Could not change into directory $repo"
  # all repos with %%SUBSPEC%% have 'src', change into it
  cd src
  STATUS=$?
  checkStatus "Could not change into src directory of $repo"
  # create subspecs
  iterateSubspecs "  " "" ""
  STATUS=$?
  checkStatus "Could not create the sub-spec for $repo"
  template=${template/\%%SUBSPEC%%/$subspec_template}
  popd
  STATUS=$?
  checkStatus "Could not change back into cocoapods directory"
else
  # no subspec necessary
  echo "Value '%%SUBSPEC%%' not found within template.podspec in $repo_cocoa"
fi

version="${tag/\v/}"
echo "Version is $version"

template=${template/\%%VERSION%%/$version}

mkdir $version
STATUS=$?
checkStatus "Could not mkdir $version within $repo_cocoa"

cd $version
STATUS=$?
checkStatus "Could not change into $version within $repo_cocoa"

touch "$repo_name.podspec"
STATUS=$?
checkStatus "Could not create podspec file $repo_name.podspec within $repo_cocoa/$version"

echo "$template" > "$repo_name.podspec"
STATUS=$?
checkStatus "Could not save template into podspec file $repo_name.podspec within $repo_cocoa/$version"

#git add -A
#STATUS=$?
#checkStatus "Could not 'git add -A' $repo_name.podspec within $repo_cocoa/$version"

#git commit --author="$author" -m "Creating $repo_name.podspec within $version - $message"
#STATUS=$?
#checkStatus "Could not 'git commit'"

#git push
#STATUS=$?
#checkStatus "Could not 'git push'"
