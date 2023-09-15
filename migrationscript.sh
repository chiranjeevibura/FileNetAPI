#!/bin/bash

# Define paths
input_base="/home/foldera/input"
processed_base="/home/foldera/processed"
utility_jar="your-utility.jar"

# Function to check if the Java utility is running
is_java_utility_running() {
  pgrep -f "java -jar $utility_jar" >/dev/null
}

# Function to move a batch folder to the processed directory
move_to_processed() {
  local source_folder="$1"
  local target_folder="$2"
  echo "Moving $source_folder to $target_folder"
  mv "$source_folder" "$target_folder"
}

# Function to check if any input batch folders have been modified in the last 30 minutes
check_input_folders() {
  local current_time=$(date +%s)
  local min_modified_time=$((current_time - 1800))  # 1800 seconds = 30 minutes

  for input_folder in $input_base/*/; do
    local folder_mtime=$(stat -c %Y "$input_folder")
    if ((folder_mtime <= min_modified_time)); then
      process_batch_folder "$input_folder"
    fi
  done
}

# Function to process a batch folder
process_batch_folder() {
  local batch_folder="$1"
  local manifest_file="$batch_folder/$(basename "$batch_folder").csv"
  local success_file="$manifest_file.success"
  local error_file="$manifest_file.error"
  local config_file="/home/tools/resources/config.properties"

  # Check if the Java utility is already running
  if ! is_java_utility_running; then
    # Update the config.properties file with dynamic input values
    sed -i "s|inFile=.*|inFile=$manifest_file|g" "$config_file"
    sed -i "s|outFolder=.*|outFolder=$batch_folder|g" "$config_file"

    # Start the Java utility
    echo "Starting Java utility for $batch_folder"
    java -jar "$utility_jar" "$config_file"

    # Monitor the Java utility process
    while is_java_utility_running; do
      sleep 10  # Adjust the sleep interval as needed
    done

    # Restore original config.properties file
    sed -i "s|inFile=.*|inFile=/home/tools/resources/default_input_file.txt|g" "$config_file"
    sed -i "s|outFolder=.*|outFolder=/home/tools/resources/default_output_folder|g" "$config_file"

    # Check for success or error files
    if [ -e "$success_file" ]; then
      echo "Migration for $batch_folder was successful"
      move_to_processed "$batch_folder" "$processed_base"
    elif [ -e "$error_file" ]; then
      echo "Migration for $batch_folder encountered errors"
      # You can handle error cases here if needed
    else
      echo "No success or error file found for $batch_folder. Check the Java utility logs."
    fi
  fi
}

# Main loop to continuously monitor input folders
while true; do
  check_input_folders
  sleep 600  # Check every 10 minutes (adjust as needed)
done


************************************

# Function to process a batch folder
process_batch_folder() {
  local batch_folder="$1"

  # Extract the relevant parts from the batch folder name
  batch_folder_name=$(basename "$batch_folder")
  folder_parts=($(echo "$batch_folder_name" | tr '_' ' ')) # Split by underscore

  # Ensure that the folder name has enough parts to construct the manifest file name
  if [ "${#folder_parts[@]}" -ge 4 ]; then
    date_part="${folder_parts[2]}"
    time_part="${folder_parts[3]}"

    # Construct the manifest file name
    manifest_file_name="${folder_parts[0]}_${folder_parts[1]}_History_${date_part}_${time_part}.txt"

    # Combine the batch folder path and the manifest file name
    manifest_file="$batch_folder/$manifest_file_name"

    # ... Rest of your script ...
  else
    echo "Invalid folder name: $batch_folder_name"
  fi
}

