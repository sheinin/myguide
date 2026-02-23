#!/bin/bash

# Loop through all items in the current directory
declare -i count
for itm in pics/*; do
 for item in $itm/*; do
  # Check if the item is a directory
  if [ -d "$item" ]; then
    #echo "Processing directory: $item"
    for it in "$item"/*; do
     if [ -d "$it" ]; then
       ((count += 1))
       f=$(ls $it/*.jpg)
       #echo $f
       output=$(echo "${f##*/}" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9_.]/_/g' | sed 's/^[0-9]/_/')
       cp $it/*.jpg ./pic/$output
       echo ${output%.*}
       echo "###"
       cat $it/*escription.txt
       echo "###"
       cat $it/*formation.txt
       echo "***"
     fi
    done
    # Perform operations on the directory here
    # Example: cd "$item" && run_command && cd ..
  fi
 done
done