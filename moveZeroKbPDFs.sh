moveZeroKbPDFs() {
    input_directory="path/to/input_directory"
    error_directory="path/to/error_directory"
    manifest_file="manifest.txt"
    modified_manifest_file="manifest_modified.txt"

    # Create the error directory if it doesn't exist
    mkdir -p "$error_directory"

    # Read the manifest file and process each line
    while IFS='|' read -r col1 col2 pdf_name col4 col5; do
        pdf_path="$input_directory/$pdf_name.pdf"

        # Check if the PDF file exists and has zero size
        if [ -e "$pdf_path" ] && [ ! -s "$pdf_path" ]; then
            # Move zero-kb PDF file to the error directory
            mv "$pdf_path" "$error_directory/"

            # Exclude the row from the modified manifest file
            sed -i "/$pdf_name/d" "$modified_manifest_file"
        fi
    done < "$input_directory/$manifest_file"
}

# Call the function
moveZeroKbPDFs
