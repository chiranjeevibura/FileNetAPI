#!/bin/bash

# Start time
start_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "Start Time: $start_time"

# Run the Java program
java CsvProcessor

# End time
end_time=$(date +"%Y-%m-%d %H:%M:%S")
echo "End Time: $end_time"

# Calculate total time
start_timestamp=$(date -d "$start_time" +%s)
end_timestamp=$(date -d "$end_time" +%s)
total_seconds=$((end_timestamp - start_timestamp))

# Format the total time
total_time=$(date -u -d @"$total_seconds" +"%H:%M:%S")

echo "Total Time: $total_time"
