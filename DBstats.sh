#!/bin/bash

# CSV files
CSV_FILES=("file1.csv" "file2.csv" "file3.csv" "file4.csv" "file5.csv")

# Output file for stats
STATS_FILE="stats.txt"
UNIQUE_ACCOUNTS_FILE="unique_accounts.txt"

# Create or truncate output files
> "$STATS_FILE"
> "$UNIQUE_ACCOUNTS_FILE"

# Process each CSV file
for file in "${CSV_FILES[@]}"; do
    # Get total account numbers
    total_accounts=$(awk -F',' 'NR > 1 {print $1}' "$file" | wc -l)

    # Get unique account numbers
    unique_accounts=$(awk -F',' 'NR > 1 {print $1}' "$file" | sort -u)

    # Append stats to the output file
    echo "File: $file" >> "$STATS_FILE"
    echo "Total Account Numbers: $total_accounts" >> "$STATS_FILE"
    echo "Unique Account Numbers: $unique_accounts" >> "$STATS_FILE"
    echo "---" >> "$STATS_FILE"

    # Append unique account numbers to the output file
    echo "$unique_accounts" >> "$UNIQUE_ACCOUNTS_FILE"
done

echo "Stats extraction completed successfully!"
______________

#!/bin/bash

# CSV files
CSV_FILES=("file1.csv" "file2.csv" "file3.csv" "file4.csv" "file5.csv")

# Output file for stats
STATS_FILE="stats.txt"
UNIQUE_ACCOUNTS_FILE="unique_accounts.txt"

# Create or truncate output files
> "$STATS_FILE"
> "$UNIQUE_ACCOUNTS_FILE"

# Process each CSV file
for file in "${CSV_FILES[@]}"; do
    # Get total account numbers
    total_accounts=$(awk -F',' 'NR > 1 {print $1}' "$file" | wc -l)

    # Get unique account numbers
    unique_accounts=$(awk -F',' 'NR > 1 {print $1}' "$file" | sort -u)

    # Append stats to the output file
    echo "File: $file" >> "$STATS_FILE"
    echo "Total Account Numbers: $total_accounts" >> "$STATS_FILE"
    echo "Unique Account Numbers: $unique_accounts" >> "$STATS_FILE"

    # Get total unique account numbers class-wise
    class_wise_counts=$(awk -F',' 'NR > 1 {print $1}' "$file" | sort | uniq -c)
    echo -e "Class-wise Counts:\n$class_wise_counts" >> "$STATS_FILE"

    echo "---" >> "$STATS_FILE"

    # Append unique account numbers to the output file
    echo "$unique_accounts" >> "$UNIQUE_ACCOUNTS_FILE"
done

echo "Stats extraction completed successfully!"
