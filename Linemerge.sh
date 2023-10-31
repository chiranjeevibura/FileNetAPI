#!/bin/bash

# Initialize a variable to store merged lines
merged_line=""

# Read the input file line by line
while IFS= read -r line; do
    # Check if the line does not start with '|'
    if [[ ! $line =~ ^\| ]]; then
        # Merge the line with the previous line
        merged_line+="$line "
    else
        # If the line starts with '|', print the merged line (if any)
        # and then the current line
        if [ -n "$merged_line" ]; then
            echo "$merged_line"
            merged_line=""
        fi
        echo "$line"
    fi
done < input.txt  # Replace 'input.txt' with your file name

# Print the last merged line (if any)
if [ -n "$merged_line" ]; then
    echo "$merged_line"
fi
