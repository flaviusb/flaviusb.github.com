#!/bin/bash
scalac -classpath snakeyaml-1.7.jar blog-post.scala
scala -classpath snakeyaml-1.7.jar:. blog_post $1 $2 $3 $4 $5 $6 $7 $8 $9
