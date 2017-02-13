#!/bin/bash
set -u
set -e

path=$PWD/"src/main/resources"

touch "$path/google.conf"

cat > "$path"/google.conf <<-EOF
google {
   key = "$GOOGLE_KEY"
}
EOF
