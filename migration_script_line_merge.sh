#!/bin/bash

# Input and output file names
input_file="input.txt"
output_file="output.txt"

# Count the number of rows in the input file excluding the header
total_rows=$(($(grep -c "|" "$input_file") - 1))

# Count the number of rows starting with "|"
matching_rows=$(grep -c "^|" "$input_file")

# Validate if the counts match
if [ "$total_rows" -eq "$matching_rows" ]; then
    echo "Number of rows in the input file matches the number of rows starting with '|'. Exiting without processing."
    exit 0
fi

# Variable to store merged lines
merged_line=""

# Read input file line by line
while IFS= read -r line; do
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

# Read the output file, extract column values after the second pipe "|"
pdf_names=$(awk -F'|' 'NR > 1 {print $3}' "$output_file")

# Count total PDF names
total_pdf=$(echo "$pdf_names" | wc -w)

# Count unique PDF names
unique_pdf=$(echo "$pdf_names" | tr ' ' '\n' | sort -u | wc -l)

# Compare total PDF names and unique PDF names and print appropriate statement
if [ "$total_pdf" -eq "$unique_pdf" ]; then
    echo "Total PDF names: $total_pdf"
    echo "Unique PDF names: $unique_pdf"
    echo "Total PDF names and unique PDF names match."

    # Define a variable to store the count of PDF files in the input directory (excluding subdirectories)
    input_pdf_count=$(find . -maxdepth 1 -type f -name "*.pdf" | wc -l)

    # Compare input PDF count with unique PDF count and proceed if they match
    if [ "$input_pdf_count" -eq "$unique_pdf" ]; then
        echo "Number of PDF files in the input directory matches the unique PDF names count. Proceeding to migrate documents."

        # Add your Java process command here to migrate documents
        # For example: java -jar YourMigrationTool.jar "$input_file" "$output_file"

        echo "Documents migrated successfully."
    else
        echo "Number of PDF files in the input directory does not match the unique PDF names count. Aborting migration."
    fi
else
    echo "Total PDF names: $total_pdf"
    echo "Unique PDF names: $unique_pdf"
    echo "Total PDF names and unique PDF names do not match. Difference: $(($total_pdf - $unique_pdf))"
fi
