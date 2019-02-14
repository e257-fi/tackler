#!/bin/bash

exe_dir=$(dirname $0)

reports="balance balance-group register"
#reports="balance"

sets="1E3 1E4 1E5 1E6"
#sets="1E5"


#versions="0.4.1 0.5.0 0.6.0 0.7.0 0.8.0 0.9.0 0.10.0"
versions="0.23.0-SNAPSHOT"

fltStr="base64:"$(cat << EOF | base64 --wrap=0
{ "txnFilter": { "TxnFilterAND" : { "txnFilters" : [ { "TxnFilterTxnCode": { "regex": "#.*" }},  { "TxnFilterTxnDescription": { "regex": "txn-.*" } } ] } } }
EOF
)


for v in $versions; do
for s in $sets; do
for r in $reports; do

for frmt in txt json "txt json"; do
#for frmt in txt; do

for filter in "" "$fltStr"; do
#for filter in ""; do

if [ -n "$filter" ]; then
       flt="filter"
else
       flt="all"
fi

echo "run: $v fs $s $r $frmt $flt"
$exe_dir/perf-run.sh dist/tackler-cli-$v.jar fs $s $r "$frmt" "$filter"

done
done
done
done
done


for v in $versions; do
for s in $sets; do
for r in balance; do
for frmt in txt; do

filter=""

echo "run: $v git $s $r $frmt all"
$exe_dir/perf-run.sh dist/tackler-cli-$v.jar git $s $r "$frmt" "$filter"

done
done
done
done

