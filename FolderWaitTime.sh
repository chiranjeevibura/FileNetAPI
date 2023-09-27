#!/bin/bash

# Define an array of parent folders
parent_folders=("/hosting/a/" "/hosting/b/" "/hosting/c/")

# Function to calculate the time difference in hours
calculate_time_difference() {
    local folder_path="$1"
    local current_time=$(date +%s)
    local folder_modification_time=$(stat -c %Y "$folder_path")
    local time_difference=$((current_time - folder_modification_time))
    local hours=$((time_difference / 3600)) # Convert seconds to hours
    echo "$hours"
}

# Function to list folder counts and waiting times for a parent folder
list_folders_and_waiting_times() {
    local parent_folder="$1"
    echo "Parent Folder: $parent_folder"

    # Loop through subfolders
    for subfolder in "$parent_folder"/*; do
        if [ -d "$subfolder" ]; then
            local subfolder_name=$(basename "$subfolder")
            local folder_count=$(find "$subfolder" -maxdepth 1 -type d | wc -l)
            local waiting_time=$(calculate_time_difference "$subfolder")
            echo "  Subfolder $subfolder_name: Count $folder_count, Time Waiting $waiting_time hours"
        fi
    done
}

# Call the function for each parent folder
for folder in "${parent_folders[@]}"; do
    list_folders_and_waiting_times "$folder"
done
