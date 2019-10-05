# vim: tabstop=4 shiftwidth=4 softtabstop=4 smarttab expandtab autoindent
#!/bin/bash

ts=$1
shift
sed -i 's/:page-last_modified_at: 20..-..-.. 00:00:00/:page-last_modified_at: '$ts' 00:00:00/' "$@"
