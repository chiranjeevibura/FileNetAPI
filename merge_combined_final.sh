#!/bin/bash

input_file="input.txt"
output_file="output.txt"
temp_file="temp.txt"

# Variable to store merged lines
merged_line=""
prev_line=""

# Read input file line by line and apply merge logic to create output file directly
while I IFS= read -r line || [ -n "$line" ]; do
    if [[ $line == "|"* ]]; then
        if [[ -n $prev_line ]]; then
            count_prev=$(grep -o '|' <<< "$prev_line" | wc -l)
            count_current=$(grep -o '|' <<< "$line" | wc -l)

            # Merge lines if both have less than 4 '|' characters
            if [[ $count_prev -lt 4 && $count_current -lt 4 ]]; then
                merged_line="$prev_line $line"
                prev_line=$merged_line
                continue
            fi

            echo "$prev_line" >> "$output_file"
        fi
        prev_line=$line
    else
        prev_line="$prev_line$line"
    fi
done < "$input_file"

# Append the last line if it's not merged
if [[ -n $prev_line ]]; then
    echo "$prev_line" >> "$output_file"
fi
