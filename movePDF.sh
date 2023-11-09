#!/bin/bash

# Input CSV file path and delimiter
csv_file="path/to/your/csvfile.csv"
delimiter="|"

# Input and output directories
input_dir="path/to/input_directory"
output_dir="path/to/output_directory"

# Check if the CSV file exists and is readable
if [ -f "$csv_file" ] && [ -r "$csv_file" ]; then
    # Loop through the CSV file, reading the 4th column
    while IFS=$delimiter read -r col1 col2 col3 pdf_name || [ -n "$col1" ]; do
        if [ ! -z "$pdf_name" ] && [ -f "$input_dir/$pdf_name" ]; then
            mv "$input_dir/$pdf_name" "$output_dir/"
            echo "Moved $pdf_name to $output_dir"
        fi
    done < "$csv_file"

    # Move the input CSV file to the output directory
    if [ -f "$csv_file" ]; then
        mv "$csv_file" "$output_dir/"
        echo "Moved $csv_file to $output_dir"
    fi
fi
