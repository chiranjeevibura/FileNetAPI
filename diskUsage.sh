#!/bin/bash

# Directory to monitor
monitor_dir="/host/logs/"

# Disk usage threshold (in percentage)
threshold=50

# Function to nullify log files matching the specified pattern
nullify_logs() {
  local log_pattern="$1"
  find "$monitor_dir" -type f -name "$log_pattern" -exec truncate -s 0 {} +
}

# Function to check disk usage
check_disk_usage() {
  local usage=$(df -P "$monitor_dir" | awk 'NR==2 { print $5 }' | tr -d '%')
  if [ "$usage" -gt "$threshold" ]; then
    echo "Disk usage is above threshold ($usage%), nullifying logs..."
    nullify_logs "tx--*-ltx*.log0*"
  else
    echo "Disk usage is below threshold ($usage%). No action needed."
  fi
}

# Main script
check_disk_usage
----



#!/bin/bash

# Directory to monitor
monitor_dir="/hosting/logs/wls/tx-prod-wls/"

# Disk usage threshold (in percentage)
threshold=50

# Function to nullify log files matching the specified patterns
nullify_logs() {
  local patterns=("$@")  # Store all patterns in an array
  for pattern in "${patterns[@]}"; do
    find "$monitor_dir" -type f -name "$pattern" -exec truncate -s 0 {} +
  done
}

# Function to check disk usage
check_disk_usage() {
  local usage=$(df -P "$monitor_dir" | awk 'NR==2 { print $5 }' | tr -d '%')
  if [ "$usage" -gt "$threshold" ]; then
    echo "Disk usage is above threshold ($usage%), nullifying logs..."
    # Define patterns to nullify
    patterns=("tx-prod-p8-*-ltx*.log0*" "other-pattern*.log")
    nullify_logs "${patterns[@]}"
  else
    echo "Disk usage is below threshold ($usage%). No action needed."
  fi
}

# Main script
check_disk_usage
