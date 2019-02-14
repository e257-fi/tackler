#!/bin/bash

exe_path="$1"
storage="$2"
trg="$3"
report="$4"
formats="$5"
filter="$6"


build="$(java -jar $exe_path --version | sed 's/^Version: //')"
version="$(echo $build | sed 's/\([^ ]\+\) \[.*/\1/')"

if [ "$storage" = "fs" ];then
	if [ "$version" = "0.4.1" ]; then
   		fs=txn
	else
   		fs=fs
	fi
	inputSelector=" --input.$fs.dir data/txns-$trg --input.$fs.glob **.txn "


elif [ "$storage" = "git" ]; then
	inputSelector="--input.git.ref txns-$trg"
else
	echo "unkonwn storage: $storage"
	exit 1
fi



if [ -n "$filter" ]; then 
       flt="flt"
       fltOpts="--api-filter-def ${filter}"
else
       flt="all"
       fltOpts=
fi


report_file=results/hwXX/$version-perf-$flt-$storage-$trg-$report-"$(echo $formats | tr ' ' '_')".txt

(
echo "exe: $exe_path"
echo "build: $build"
echo "storage: $storage"
echo "set: $trg"
echo "version: $version"
echo "report: $report"
echo "formats: $formats"
echo "filter: $filter"
echo ""


for i in 1 2 3 4 5; do 

	/usr/bin/time -f "\nreal\t%es\nuser\t%Us\nsys\t%Ss\nmem\t%Mk (max)\ncpu\t%P" \
	java -Xmx8G -Xms8G -jar "$exe_path" \
	--cfg perf-$storage.conf \
	$inputSelector \
	--output out/perf-$storage-$trg-$flt \
	--reporting.console false \
	--reporting.formats $formats \
	--reporting.reports $report \
	$fltOpts
	echo

done
) > "$report_file"  2>&1

# clean up path prefix
sed -i 's@/.*perf/@perf/@' "$report_file"

