#!/bin/bash
# vim: tabstop=4 shiftwidth=4 smarttab expandtab softtabstop=4 autoindent



usage () {
    echo "This will initialize git repository"
    echo "and populate it with test data"
    echo
	echo "Usage: $0 <repo name> <1E2 | 1E3 | 1E4 | 1E5 | 1E6>"
}

if [ $# != 2 ]; then
    usage
    exit 1
fi

repo_name=$1

name=txns-$2
store="../$name"

if [ ! -d $name ]; then
	echo "Error: $name not found"
    usage
	exit 1
fi

if [ ! -d $repo_name ]; then
    git init --bare "$repo_name.git"
    git clone "$repo_name.git"
fi

cd $repo_name

if [ ! -e readme.txt ]; then
    echo "Tackler test repository for git storage backend" > readme.txt
    echo "See different branches for available sets" >> readme.txt
    echo >> readme.txt

    git config user.name tackler
    git config user.email "accounting@example.com"

    git add readme.txt
    git commit -m 'Initial readme for master'
    git push --set-upstream origin master
fi

git checkout master

echo " * $name" >> readme.txt
git add readme.txt
git commit -m "$name" readme.txt
git push

git checkout -b $name

echo "set: $name" > "info.txt"
git add "info.txt"
git commit -m "$name: initial"
 
mkdir -p txns
mkdir -p txns/2016

for m in 01 02 03 04 05 06 07 08 09 10 11 12; do

    src="$store/2016/$m"

    echo "Perf: start $name, round: $m"

    if [ ! -d "$src" ]; then
        echo "Perf: skip  $name, round: $m"
        echo
        continue
    fi
    cp -a "$src" txns/2016/
    git add txns
    git commit -m "$name: 2016/$m"
    git gc

    # make sure that git time stamps are distinct
    echo "Perf: done $name, round: $m"
    echo
    sleep 3
done

git push --set-upstream origin $name

