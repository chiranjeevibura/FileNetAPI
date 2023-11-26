#!/bin/bash

# Set up initial conditions
folder="/path/to/your/folder"
processed_folder="$folder/processed"
manifest_file="manifest.csv"
output_file="output.txt"
error_file="$output_file.error"
success_file="$output_file.success"
backup_folder="$folder/backup"
java_process_command="java -jar YourMigrationTool.jar"
recipient_email="your@email.com"  # Change this to the desired email address

while true; do
    # Wait for 60 seconds to check for new files
    sleep 60

    # Get the count of PDF files and the manifest
    pdf_files=( $(find "$folder" -maxdepth 1 -name "*.pdf" -mmin +30 2>/dev/null) )
    manifest_file_path="$folder/$manifest_file"

    # Check if there are PDF files and a manifest
    if [ ${#pdf_files[@]} -gt 0 ] && [ -f "$manifest_file_path" ]; then
        # Get counts for validation
        total_pdf_files=${#pdf_files[@]}
        manifest_pdf_count=$(awk -F"|" 'NR>1 && $4 != "" {print $3}' "$manifest_file_path" | sort -u | wc -l)
        valid_manifest="Invalid"
        
        # Validate the manifest based on the condition in the 4th column
        if [ $total_pdf_files -eq $manifest_pdf_count ]; then
            null_count=$(awk -F"|" 'NR>1 && $4 == "" {print $3}' "$manifest_file_path" | wc -l)
            if [ $null_count -eq 0 ]; then
                valid_manifest="Valid"
            fi
        fi

        if [ $valid_manifest = "Valid" ]; then
            # Run the Java process
            $java_process_command "$manifest_file_path" "$output_file" > "$error_file" 2>&1

            java_exit_status=$?
            if [ $java_exit_status -eq 0 ]; then
                error_count=$(tail -n +2 "$error_file" | wc -l)
                if [ "$error_count" -eq 0 ]; then
                    mkdir -p "$processed_folder"
                    find "$folder" -maxdepth 1 -type f \( -name "*.pdf" -o -name "$manifest_file" \) -exec mv {} "$processed_folder" \;

                    echo "Migration successful. Files moved."

                    # Send success mail with summary
                    subject="Migration Successful"
                    body="Hello,\n\nThe migration was successful.\n\nSummary:\n"
                    body+="Number of PDF files processed: $total_pdf_files\n"
                    body+="Manifest validation: $valid_manifest\n"
                    body+="Number of files with errors: $error_count\n"

                    echo -e "$body" | mail -s "$subject" "$recipient_email"
                else
                    echo "Migration completed with errors. Error file contains data."

                    # Send failure mail with error summary
                    subject="Migration Failed"
                    body="Hello,\n\nThe migration encountered errors.\n\nError Summary:\n"
                    body+="Number of errors: $error_count\n"

                    echo -e "$body" | mail -s "$subject" "$recipient_email"
                fi
            else
                echo "Java process failed. Please check logs."

                # Send failure mail with Java process failure
                subject="Java Process Failed"
                body="Hello,\n\nThe Java process failed. Please check the logs for details."

                echo -e "$body" | mail -s "$subject" "$recipient_email"
            fi
        else
            echo "Manifest validation failed."

            # Send failure mail with manifest validation failure
            subject="Manifest Validation Failed"
            body="Hello,\n\nThe manifest validation failed."

            echo -e "$body" | mail -s "$subject" "$recipient_email"
        fi
    fi
done
