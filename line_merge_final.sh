#!/bin/bash

# Input and output file names
input_file="input.txt"
output_file="output.txt"
processed_folder="processed"
output_folder="output"
error_file="$output_file.error"
backup_folder="backup"
java_process_command="java -jar YourMigrationTool.jar"  # Update this with the correct Java process command

# Ensure the input file exists and is readable
if [ ! -f "$input_file" ] || [ ! -r "$input_file" ]; then
    echo "Error: Input file '$input_file' not found or not readable."
    exit 1
fi

# Backup the original input file
mkdir -p "$backup_folder"
cp "$input_file" "$backup_folder/$input_file_$(date +%Y%m%d%H%M%S)"

# Step 1-4: Validate input, process data, create output file
total_rows=$(($(grep -c "|" "$input_file") - 1))
matching_rows=$(grep -c "^|" "$input_file")

if [ "$total_rows" -ne "$matching_rows" ]; then
    echo "Error: Number of rows in the input file does not match the number of rows starting with '|'. Exiting."
    exit 1
fi

cp "$input_file" "$output_file"

merged_line=""
while IFS= read -r line; do
    if [[ $line == "|"* ]]; then
        if [[ ! -z $merged_line ]]; then
            echo "$merged_line" >> "$output_file"
        fi
        merged_line="$line"
    else
        merged_line="$merged_line$line"
    fi
done < "$input_file"

if [[ ! -z $merged_line ]]; then
    echo "$merged_line" >> "$output_file"
fi

# Step 5-8: Validate PDF names, compare counts, and execute Java process
pdf_names=$(awk -F'|' 'NR > 1 {print $3}' "$output_file")
total_pdf=$(echo "$pdf_names" | wc -w)
unique_pdf=$(echo "$pdf_names" | tr ' ' '\n' | sort -u | wc -l)
input_pdf_count=$(find . -maxdepth 1 -type f -name "*.pdf" | wc -l)

if [ "$input_pdf_count" -ne "$unique_pdf" ]; then
    echo "Error: Number of PDF files in the input directory does not match the unique PDF names count. Difference: $(($unique_pdf - $input_pdf_count))"
    exit 1
fi

# Step 9-11: Validate Java process, move files, and perform error handling
$java_process_command "$input_file" "$output_file" > "$error_file" 2>&1
java_exit_status=$?

if [ $java_exit_status -eq 0 ]; then
    error_count=$(($(tail -n +2 "$error_file" | wc -l)))
    if [ "$error_count" -eq 0 ]; then
        echo "Success: Java process completed and error file is empty. Proceeding to move files."

        mkdir -p "$processed_folder"
        find . -maxdepth 1 -type f \( -name "*.pdf" -o -name "*.csv" \) -exec mv {} "$processed_folder" \;

        mkdir -p "$output_folder"
        mv "$output_file.success" "$output_folder/"
        mv "$error_file" "$output_folder/"

        echo "Files moved successfully."
    else
        echo "Error: Java process completed, but error file is not empty. Aborting further processing."
        exit 1
    fi
else
    echo "Error: Java process encountered an issue. Aborting further processing."
    exit 1
fi

echo "Script execution completed successfully."
