import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class VaultCommands {
    public static void main(String[] args) {
        // Load properties from file
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Extract values from properties file
        String vaultUrl = properties.getProperty("vault.url");
        String roleId = properties.getProperty("role.id");
        String secretId = properties.getProperty("secret.id");
        String namespace = properties.getProperty("namespace");

        try {
            // Run the first curl command to log in to Secrets Vault and extract client token
            String loginCommand = String.format("curl -X POST %s/vl/auth/approle/login -H 'content-type: application/json' " +
                    "-H 'x-vault-namespace: %s' -d '{\"role_id\":\"%s\", \"secret_id\":\"%s\"}'", vaultUrl, namespace, roleId, secretId);
            String clientToken = executeCurlCommandAndGetToken(loginCommand);

            // Run the second curl command to rotate the password
            String rotateCommand = String.format("curl -X POST %s/vl/oracle/rotate-role/CHIRU_LAB -H 'x-vault-namespace:%s' " +
                    "-H 'x-vault-token: %s'", vaultUrl, namespace, clientToken);
            // Execute rotateCommand

            // Run the third curl command to retrieve the password and extract it
            String retrieveCommand = String.format("curl -X GET %s/vl/oracle/static-creds/CHIRU_LAB -H 'x-vault-namespace:%s' " +
                    "-H 'x-vault-token: %s'", vaultUrl, namespace, clientToken);
            String retrievedPassword = executeCurlCommandAndGetPassword(retrieveCommand);

            // Print the retrieved password
            System.out.println("Retrieved Password: " + retrievedPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String executeCurlCommandAndGetToken(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("\"client_token\"")) {
                    // Extract client token from the line
                    return line.split(":")[1].replace("\"", "").trim();
                }
            }
        }

        return null; // Handle if client token is not found
    }

    private static String executeCurlCommandAndGetPassword(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("\"password\"")) {
                    // Extract password from the line
                    return line.split(":")[1].replace("\"", "").trim();
                }
            }
        }

      

        return null; // Handle if password is not found
    }
}
config.properties
  vault.url=https://vault-dev.abc.com
role.id=rolePQR
secret.id=a03b919d-b4df-f101-04fd-3£2606e73523
namespace=ait/11/dbname//dev
