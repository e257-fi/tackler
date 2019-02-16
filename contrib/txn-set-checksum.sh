#!/bin/sh
# vim: tabstop=4 shiftwidth=4 softtabstop=4 smarttab expandtab autoindent
#
# Copyright 2019 E257.FI
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#############################################################################
#
#
# Tackler example script: verify txn set checksum
#

if [ $# -ne 1 ]; then
    echo "Usage: $0 <txns dir>"
    exit 1
fi

txns_dir="$1"

(
    find "$txns_dir"  -type f -name '*.txn' |\
    xargs -n10 grep -h ';:uuid:'
) |\
    sed -E 's/[[:space:]]+;:uuid:[[:space:]]+([a-fA-F0-9-]+)[[:space:]]*/\1/' |\
    tr 'A-F' 'a-f' |\
    sort |\
    sha256sum |\
    sed -E 's/([a-f0-9]+) +-/\1/'
