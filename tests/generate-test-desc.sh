#!/bin/bash
#
# Generate test-db entries from ScalaTest-files
#
# Expected input format of ScalaTest is (func or flat spec):
#    /**
#     * test: f0e2f23c-7cc6-4610-80c0-8f1e3a6555c7
#     */
#     ... ("test description") ... {
#
test_file="$1"
test_class="$2"

print_test() {
  local test_id="$1"
  local desc="$2"

cat << EOF
          - test:
              id: $test_id
              name: $test_class
              descriptions:
                - desc: "$desc"
EOF
}


grep --no-group-separator -A2 test: "$test_file" | \
grep -v '\*/' | \
while read tst 
do 
	read raw_desc
	desc="$(echo $raw_desc | sed 's/.*"\(.*\)".*/\1/')"
	test_id="$(echo $tst | sed 's/.*test: //')"
	print_test "$test_id" "$desc"
done
