#!/bin/bash
# vim: tabstop=4 shiftwidth=4 softtabstop=4 smarttab expandtab autoindent
#
# Generate test-db entries from dirsuite tests
#
test_file="$1"

print_test() {
    local test_name="$1"

    indent="          "
    desc_indent="${indent}      "
cat << EOF
${indent}- test:
${indent}    id: $(sed -n 's/# .*test: \(.*\)/\1/p' "$test_name")
${indent}    name: $test_name
${indent}    descriptions:
$(sed -n    's/# .*desc: \(.*\)/'"${desc_indent}"'- desc: "\1"/p' "$test_file")
EOF
}

print_test "$test_file"
