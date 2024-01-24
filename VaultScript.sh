#!/bin/bash

# Load properties from file
source config.properties

# Run the first curl command to log in to Secrets Vault and extract client token
login_output=$(curl -X POST $vault_url/vl/auth/approle/login -H "content-type: application/json" -H "x-vault-namespace: $namespace" -d "{\"role_id\":\"$role_id\", \"secret_id\":\"$secret_id\"}")

# Extract client token from the login output
client_token=$(echo "$login_output" | grep -oP '"client_token":.*?[^\\]",' | awk -F'"' '{print $4}')

# Check if client token is obtained successfully
if [ -z "$client_token" ]; then
    echo "Failed to obtain client token. Check the output of the first command."
    exit 1
fi

# Run the second curl command to rotate the password
rotate_output=$(curl -X POST $vault_url/vl/oracle/rotate-role/CHIRU_LAB -H "x-vault-namespace:$namespace" -H "x-vault-token: $client_token")

# Run the third curl command to retrieve the password and extract it
retrieve_output=$(curl -X GET $vault_url/vl/oracle/static-creds/CHIRU_LAB -H "x-vault-namespace:$namespace" -H "x-vault-token: $client_token")

# Extract password from the retrieve output
retrieved_password=$(echo "$retrieve_output" | grep -oP '"password":.*?[^\\]",' | awk -F'"' '{print $4}')

# Print the retrieved password
echo "Retrieved Password: $retrieved_password"
