#!/bin/bash
# vim: tabstop=4 shiftwidth=4 smarttab expandtab softtabstop=4 autoindent


repo_name=git-perf-repo

usage () {
    echo "This will initialize git repository: $repo_name"
    echo "and populate it with test data"
    echo
	echo "Usage: $0 <1E3 | 1E4 | 1E5 | 1E6>"
}

if [ $# != 1 ]; then
    usage
    exit 1
fi


name=txns-$1
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
    echo "Tackler performance test repository for git storage backend" > readme.txt
    echo "See different branches for available sets" >> readme.txt

    git config user.name tackler
    git config user.email "accounting@example.com"

    git add readme.txt
    git commit -m 'Initial readme for master'
    git push --set-upstream origin master
fi

git checkout master

git checkout -b $name

echo "set: $name" > "info.txt"
git add "info.txt"
git commit -m "$name: initial"
 
mkdir -p txns
mkdir -p txns/2016

for i in 01 02 03 04 05 06 07 08 09 10 11 12; do

    echo "Perf: doing $name, round: $i"
    cp -a "$store/2016/$i" txns/2016/
    git add txns
    git commit -m "$name: 2016/$i"
    git gc

    # make sure that git time stamps are distinct
    echo "Perf: done $name, round: $i"
    sleep 3
done

git push --set-upstream origin $name

