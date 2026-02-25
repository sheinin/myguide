#!/bin/bash

# Loop through all items in the current directory
cd ../../../GroceryStoreDataset/dataset/pics
declare -i count
for itm in ./*; do
 for item in $itm/*; do
  # Check if the item is a directory
  if [ -d "$item" ]; then
    #echo "Processing directory: $item"
    for it in "$item"/*; do
     if [ -d "$it" ]; then
       f=$(ls $it/*.jpg)
       output=$(echo "${f##*/}" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9_.]/_/g' | sed 's/^[0-9]/_/')
       echo -e "${itm##*/}\t${item##*/}\t${it##*/}\t${output%.*}#$(cat $it/*escription.txt)|$(cat $it/*formation.txt)"
       
       #cp $it/*.jpg ./pic/$output
     #  echo ${output%.*}
     #  echo "###"
     #  cat $it/*escription.txt
     #  echo "###"
     #  cat $it/*formation.txt
      echo "***"
     else
#       echo -e "${itm##*/}\t${item##*/}\t${it##*/}"
       f=$(ls $item/*.jpg)
       output=$(echo "${f##*/}" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9_.]/_/g' | sed 's/^[0-9]/_/')
       echo -e "${itm##*/}\t${item##*/}\t${output%.*}#$(cat $item/*escription.txt)|$(cat $item/*formation.txt)"
            echo "***"
     fi
    done
    # Perform operations on the directory here
    # Example: cd "$item" && run_command && cd ..
   # else
   #   echo -e "${itm}\t${item}\t${it}"
   #   echo "*"
  fi
 done
done
