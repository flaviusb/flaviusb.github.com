#!/bin/bash

git mv unpublished/$1 _posts/`date +%Y-%m-%d-`$1
git commit unpublished/$1 _posts/`date +%Y-%m-%d-`$1 -m "Post $1"
