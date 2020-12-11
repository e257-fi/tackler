#!/bin/bash
# vim: tabstop=4 shiftwidth=4 softtabstop=4 smarttab expandtab autoindent
#
# This is a tool to check T3DB and test records
#
# Included checks are:
# - Check tests for missing ids (exec-files):
# - Check tests for duplicate ids (scalatest + exec-files):
# - Check T3DB YAML validity:
# - Check T3DB for duplicate test ids:
# - Check T3DB for non-exist refids:
# - Check T3DB for non-exist parents:
# - Cross check T3DB and tests ids:
# - Check JSON validity:
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
t3db_10="$db_dir/tests-1010.yml"
t3db_11="$db_dir/tests-1011.yml"
t3db_12="$db_dir/tests-1012.yml"


T3DBs="$t3db_00 $t3db_01 $t3db_02 $t3db_04 $t3db_05 $t3db_06 $t3db_07 $t3db_08 $t3db_09 $t3db_10 $t3db_11 $t3db_12"

rgx_test=' +test: +[[:xdigit:]]+-[[:xdigit:]]+-[[:xdigit:]]+-[[:xdigit:]]+-[[:xdigit:]]+ *$'

get_t3db_content () {
    egrep -hv '^[[:space:]]*#' $T3DBs
}

get_t3db_feature_ids () {
    get_t3db_content | egrep -A1 ' feature:' | egrep '[[:space:]]+id:' | sed -E 's/[[:space:]]+id: +//'
}

get_t3db_test_ids () {
    get_t3db_content | egrep -A1 ' (error|test):' | egrep '[[:space:]]+id:' | sed -E 's/[[:space:]]+id: +//'
}

get_test_ids () {
    (
        # exec-based tests
        find "$exe_dir" -name '*.exec' |\
            xargs egrep -h '^#'"$rgx_test"

        # unit and integration tests
        find "$exe_dir/../api/src/" "$exe_dir/../core/src/" "$exe_dir/../cli/src/" -name '*.scala' |\
            xargs egrep -h '\*'"$rgx_test"
    )|\
        sed -E 's/ +\* +test: +//' |\
        sed -E 's/^# +test: +//'
}

t3db_feature_id_lst=$(mktemp /tmp/t3db_feature_lst.XXXXXX)
trap "rm -f $t3db_feature_id_lst" 0

t3db_id_lst=$(mktemp /tmp/t3db_id_lst.XXXXXX)
trap "rm -f $t3db_id_lst" 0

test_id_lst=$(mktemp /tmp/test_id_lst.XXXXXX)
trap "rm -f $test_id_lst" 0

get_t3db_feature_ids | sort > $t3db_feature_id_lst
get_t3db_test_ids | sort > $t3db_id_lst
get_test_ids | sort > $test_id_lst

echo "Check tests for missing ids (exec-files):"
find "$exe_dir" -name '*.exec' | xargs egrep '#'"$rgx_test" -L

# this is already checked by diff, but print dups again here
echo "Check tests for duplicate ids (scalatest + exec-files):"
get_test_ids | sort | uniq -d


echo "Check T3DB YAML validity:"
for test_db in $T3DBs
do
    $sh_pykwalify -v -s  "$exe_dir/tests-schema.yml" -d  "$test_db"
done

echo "Check T3DB for duplicate test ids:"
cat $t3db_id_lst | uniq -d

echo "Check T3DB for non-exist refids:"
get_t3db_content | grep ' refid:' | sed 's/.*refid: //' | while read refid;
do
    egrep -q -L "$refid" $t3db_id_lst || echo $refid
done

echo "Check T3DB for non-exist parents:"
get_t3db_content | grep ' parent:' | sed 's/.*parent: //' | while read parent;
do
    egrep -q -L "$parent" $t3db_feature_id_lst || echo $parent
done

echo "Cross check T3DB and tests ids:"

diff -u $t3db_id_lst $test_id_lst | grep -v -- '---' | grep '^-' |\
    while read raw_uuid; do
        uuid=$(echo $raw_uuid | sed 's/^-//')
        echo "In T3DB, no test: $(echo $uuid | sed 's/^-//')"
        grep $uuid $T3DBs
    done

diff -u $t3db_id_lst $test_id_lst | grep -v '+++' | grep '^\+' |\
    while read raw_uuid; do
        uuid=$(echo $raw_uuid | sed 's/^+//')
        echo "In test, no T3DB: $uuid"
        (
            find $exe_dir/ -name '*.exec'
            find $exe_dir/../api/src/ $exe_dir/../core/src/ $exe_dir/../cli/src/ -type f
        ) | xargs grep $uuid
    done



echo "Check JSON validity:"
find "$exe_dir" -name '*.json' -exec "$exe_dir/json_lint.py" {} \;

echo 
echo "Silence is gold"

