#!/bin/bash

# Function to rename folders and files
rename_folder_file() {
  local path="$1"

  # Go through each item in the directory
  for item in "$path"/*; do
    if [ -d "$item" ]; then
      # If it's a directory, rename it
      new_name=$(basename "$item" | sed 's/\(.*\)_20[0-9]\{2\}_[0-9]\{2\}_[0-9]\{2\}_[0-9]\{2\}_[0-9]\{2\}_[0-9]\{2\}\(.*\)/\1\2/')
      mv "$item" "$path/$new_name"
      rename_folder_file "$path/$new_name" # Recursively process subdirectories
    elif [ -f "$item" ]; then
      # If it's a file, rename it
      new_name=$(basename "$item" | sed 's/\(.*\)_20[0-9]\{2\}_[0-9]\{2\}_[0-9]\{2\}_[0-9]\{2\}_[0-9]\{2\}_[0-9]\{2\}\(.*\)/\1\2/')
      mv "$item" "$path/$new_name"
    fi
  done
}

# Define the starting directories as an array
start_directories=(
  "/path/to/dir1"
  "/path/to/dir2"
  "/path/to/dir3"
)

# Process the defined directories
for dir in "${start_directories[@]}"; do
  if [ -d "$dir" ]; then
    rename_folder_file "$dir"
  else
    echo "Error: $dir is not a valid directory."
  fi
}
