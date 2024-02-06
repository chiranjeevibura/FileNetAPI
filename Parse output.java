import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        String json = "{\"request id\": \"84de57ed-6a17-8e7d-1cb6-c553ec8d74f5\", \"lease_id\":\"\", \"renewable\": false, \"lease_duration\":0, \"data\": {\"last_vault_rotation\":\"2024-01-26T16:35:54.235292253-06:00\", \"password\" :\"xxxx-F\",\"rotation_period\":31536000, \"ttl\":30599575, \"username\": \"CHIRU_LAB\"}, \"wrap_info\" :null, \"warnings\": null, \"auth\": null}";

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode dataNode = rootNode.get("data");
            String password = dataNode.get("password").asText();
            System.out.println("Password: " + password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
