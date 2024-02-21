#!/bin/bash

# Java process name
java_process_name="java"

# Path to Java log file
java_log_file="/path/to/log/file.log"

# Email settings
email_recipient="your_email@example.com"
email_subject="Java Process Notification"

# Check if Java process is running
if ps ax | grep -v grep | grep $java_process_name > /dev/null; then
    echo "Java process is running."

    # Get last 10 lines of log file
    tail_log=$(tail -n 10 $java_log_file)

    # Send email
    echo "$tail_log" | mail -s "$email_subject" $email_recipient

else
    echo "Java process is not running."

    # Get last 10 lines of log file
    tail_log=$(tail -n 10 $java_log_file)

    # Send email
    echo "Java process is not running. Last 10 lines of log:" | mail -s "$email_subject" $email_recipient
    echo "$tail_log" | mail -s "$email_subject" $email_recipient
fi
