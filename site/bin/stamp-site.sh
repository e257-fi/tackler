#!/bin/bash

exe_dir=$(readlink -f $(dirname $(realpath $0)))

site_dir=$exe_dir/..

find $site_dir/_docs -type f -name '*.adoc' | xargs -I{} $exe_dir/stamp-page-date.sh $(date "+%Y-%m-%d") {}

