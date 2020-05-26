#!/bin/sh

#trap 'kill -TERM 0' EXIT
cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1000000 | LC_ALL=C sort -u | LC_ALL=C sort -R | LC_ALL=C sort -u

