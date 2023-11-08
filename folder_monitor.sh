#!/bin/bash

# Set up initial conditions
folder="/path/to/your/folder"
manifest_file="manifest.csv"
pdf_files=( )

# Step 1: Monitor the folder continuously and wait for files
while true; do
    # Get the number of PDF files in the directory
    pdf_files=($(ls "$folder"/*.pdf 2>/dev/null))

    # Get the count of manifest files
    manifest_count=$(ls "$folder/$manifest_file" 2>/dev/null | wc -l)

    # If there are PDF files and a manifest file, break the loop
    if [ ${#pdf_files[@]} -gt 0 ] && [ $manifest_count -gt 0 ]; then
        break
    fi

    # Wait for 60 seconds and recheck
    sleep 60
done

# After 20 minutes with no changes, send a summary mail (Step 2)
sleep 1200  # Wait for 20 minutes

# Get counts for the summary
total_pdf_files=${#pdf_files[@]}
manifest_pdf_count=$(awk -F"|" 'NR>1{if ($4) print $3}' "$folder/$manifest_file" | sort -u | wc -l)

# Check if counts match and prepare the mail body
if [ $total_pdf_files -eq $manifest_pdf_count ]; then
    subject="20-Minute Summary"
    body="Hello,\n\nHere's the 20-minute summary:\n\n"
    body+="Number of PDF files in the folder: $total_pdf_files\n"
    body+="Number of unique PDFs in the manifest: $manifest_pdf_count\n"

    # Send the email
    echo -e "$body" | mail -s "$subject" user@example.com
fi
