#!/bin/bash

# Define the three folders to check
folder1="/path/to/folder1"
folder2="/path/to/folder2"
folder3="/path/to/folder3"

# Function to calculate the time difference
calculate_time_difference() {
    local folder_path="$1"
    local current_time=$(date +%s)
    local folder_modification_time=$(stat -c %Y "$folder_path")
    local time_difference=$((current_time - folder_modification_time))
    echo "$time_difference"
}

# Function to list folder counts and waiting times
list_folders_and_waiting_times() {
    local folder="$1"
    echo "Folder: $folder"
    local folder_count=$(find "$folder" -maxdepth 1 -type d | wc -l)
    echo "Folder Count: $folder_count"

    local waiting_time=$(calculate_time_difference "$folder")
    echo "Time Waiting: $waiting_time seconds"
    echo
}

# Call the function for each folder
list_folders_and_waiting_times "$folder1"
list_folders_and_waiting_times "$folder2"
list_folders_and_waiting_times "$folder3"
