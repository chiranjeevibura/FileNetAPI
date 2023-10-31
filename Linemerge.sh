#!/bin/bash

# Input and output file names
input_file="input.txt"
output_file="output.txt"

# Variable to store merged lines
merged_line=""

# Flag to ignore the first line (header)
ignore_first_line=true

# Read input file line by line
while IFS= read -r line; do
    # Skip the first line (header)
    if [ "$ignore_first_line" = true ]; then
        echo "$line" >> "$output_file"
        ignore_first_line=false
        continue
    fi

    # Check if the line starts with "|"
    if [[ $line == "|"* ]]; then
        # If merged_line is not empty, append it to the output file
        if [[ ! -z $merged_line ]]; then
            echo "$merged_line" >> "$output_file"
        fi
        # Set merged_line to the current line
        merged_line="$line"
    else
        # If the line does not start with "|", merge it with the previous line
        merged_line="$merged_line$line"
    fi
done < "$input_file"

# Append the last merged line to the output file
if [[ ! -z $merged_line ]]; then
    echo "$merged_line" >> "$output_file"
fi

echo "Merged lines starting with '|' have been saved to $output_file."
