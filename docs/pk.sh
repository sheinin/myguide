#!/usr/bin/env bash
set -euo pipefail

ITEMS_FILE="items.txt"
SHOPS_FILE="shops.txt"

# Read all items into an array
mapfile -t ITEMS < "$ITEMS_FILE"

# Helper: get random int in [min, max] inclusive
rand_range() {
  local min=$1 max=$2
  echo $(( RANDOM % (max - min + 1) + min ))
}

echo "shop_id,item_id"

# Iterate over each shop
while IFS= read -r shop || [ -n "$shop" ]; do
  # Skip empty lines
  [ -z "$shop" ] && continue

  # Number of items for this shop (60–80)
  count=$(rand_range 60 80)

  # Shuffle item indices using shuf, then pick first $count
  # Print items[shuffled_index]
  # We first enumerate: 0..N-1, then let shuf randomize
  num_items=${#ITEMS[@]}
  if (( count > num_items )); then
    echo "Requested $count items but only have $num_items" >&2
    count=$num_items
  fi

  # Generate indices 0..(num_items-1), shuffle, take first $count
  # Then output CSV lines
  for idx in $(seq 0 $((num_items - 1)) | shuf | head -n "$count"); do
    item=${ITEMS[$idx]}
    # Skip empty lines/items
    [ -z "$item" ] && continue
    printf '%s,%s\n' "$shop" "$item"
  done

done < "$SHOPS_FILE"