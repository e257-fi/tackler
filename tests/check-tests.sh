#!/bin/bash
# vim: tabstop=4 shiftwidth=4 softtabstop=4 smarttab expandtab autoindent
#
# This tool checks meta information of tests
#
# Included checks are:
# - check syntax of test-db (tests.yaml)
# - check duplicate test-ids (test-db)
# - check refid's targets 
# - check test with missing id
# - check duplicate id's in test-vectors (exec-files)
# - check that all tests are recorded in test-db
# - check that all JSON is valid (references and output)
#
sh_pykwalify=pykwalify

exe_dir=$(dirname $(realpath $0))

db_dir="$exe_dir"

# good enough for know
t3db_00="$db_dir/tests.yml"
t3db_01="$db_dir/tests-1001.yml"
t3db_02="$db_dir/tests-1002.yml"
# t3db_03="$db_dir/tests-1003.yml" not implemented yet
t3db_04="$db_dir/tests-1004.yml"
t3db_05="$db_dir/tests-1005.yml"
t3db_06="$db_dir/tests-1006.yml"
t3db_07="$db_dir/tests-1007.yml"
t3db_08="$db_dir/tests-1008.yml"
t3db_09="$db_dir/tests-1009.yml"

T3DBs="$t3db_00 $t3db_01 $t3db_02 $t3db_04 $t3db_05 $t3db_06 $t3db_07 $t3db_08 $t3db_09"

get_t3db_content () {

    egrep -v '^[[:space:]]*#' $T3DBs
}

echo "Check test DB YAML validity:"
for test_db in $T3DBs
do
    $sh_pykwalify -v -s  "$exe_dir/tests-schema.yml" -d  "$test_db"
done

get_t3db_content | grep ' id:' | sed 's/.*id: //' | sort | uniq -d


get_t3db_content | grep ' refid:' | sed 's/.*refid: //' | while read refid;
do  
    egrep -q -L '.* id: +'$refid' *$' $T3DBs || echo $refid
done

echo "Check missing uuid:"
$exe_dir/find-missing.sh

echo "Check for duplicates:"
find "$exe_dir" -name '*.exec' | xargs sed -n 's/.*test:uuid: \(.*\)/\1/p' | sort | uniq -d

echo "Check tests with missing test-db records:"
lonelies=$(mktemp /tmp/exists-no-test-db.XXXXXX)
trap "rm -f $lonelies" 0

find "$exe_dir" -name '*.exec' |\
    xargs grep 'test:uuid:' |\
    sed 's/.*test:uuid: //' |\
    while read uuid; do
        echo "$(grep -c $uuid $T3DBs): $uuid"
    done |\
    grep '^0:' |\
    sed 's/^0: //' > $lonelies

find . -name '*.exec' | xargs grep -f $lonelies -l

echo "Check JSON validity:"
find "$exe_dir" -name '*.json' -exec "$exe_dir/json_lint.py" {} \;

echo 
echo "Silence is gold"

