Got it. We can modify the script to only consider Java processes belonging to the current user. Here's the updated script:

```bash
#!/bin/bash

# Function to check if a process is a zombie or orphan
check_process_status() {
    pid=$1
    # Check if the process is a zombie or orphan
    status=$(ps -o stat= -p $pid | awk '{print $1}')
    if [ "$status" == "Z" ] || [ "$status" == "X" ]; then
        return 0
    else
        return 1
    fi
}

# Function to send email
send_email() {
    subject="$1"
    body="$2"

    # Use mail command to send email
    echo "$body" | mail -s "$subject" your_email@example.com
}

# Get current user's username
current_user=$(whoami)

# Get list of Java processes belonging to current user
java_processes=$(jcmd | grep -E "^[0-9]+\s+\($current_user\)" | awk '{print $1}')

# Iterate over each process
for pid in $java_processes; do
    # Check if the process is a zombie or orphan
    if check_process_status $pid; then
        # Get heap memory usage before killing the process
        before_heap_usage=$(jcmd $pid GC.heap_info | grep -E 'used_space' | awk '{print $NF}')

        # Kill the process
        kill -9 $pid

        # Check if the process was killed successfully
        if [ $? -eq 0 ]; then
            # Get heap memory usage after killing the process
            after_heap_usage=$(jcmd $pid GC.heap_info | grep -E 'used_space' | awk '{print $NF}')

            # Send email with status and memory usage information
            subject="Java Process ($pid) Killed"
            body="Before Heap Memory Usage: $before_heap_usage\nAfter Heap Memory Usage: $after_heap_usage"
            send_email "$subject" "$body"
        fi
    fi
done
```

This script will now only consider Java processes belonging to the current user and perform the specified actions. Make sure to replace `your_email@example.com` with your actual email address.
