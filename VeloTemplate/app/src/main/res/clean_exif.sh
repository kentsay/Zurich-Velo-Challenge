#!/bin/bash
FOLDER="./drawable*/*.png"
for f in $FOLDER
do
  echo $f
  exiftool -all= $f
done
