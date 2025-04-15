import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

public class LoadBalancerStatusTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // spotless:off
    @Test
    public void testInvalidRequestBodyEmpty() throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/aws/load-balancer/service/status"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Expected HTTP status 400");

        JsonNode jsonResponse = objectMapper.readTree(response.body());

        assertEquals("ERROR", jsonResponse.get("status").asText(), "Expected status to be 'ERROR'");

        JsonNode alarms = jsonResponse.get("alarms");
        assertNotNull(alarms, "Alarms should not be null");
        assertTrue(alarms.isArray() && !alarms.isEmpty(), "Alarms should contain at least one error");

        JsonNode firstAlarm = alarms.get(0);
        assertEquals("REQUEST_ERROR", firstAlarm.get("code").asText(), "Alarm code should be 'REQUEST_ERROR'");
    }

    @Test
    public void testInvalidRequestIncorrectData() throws IOException, InterruptedException {
        String requestBody =
                "{\"loadBalancerName\": \"123456789\", \"instanceId\": \"123456789\", \"region\": \"123456789\", \"port\": \"123456789\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/aws/load-balancer/service/status"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Expected HTTP status 500");

        JsonNode jsonResponse = objectMapper.readTree(response.body());

        assertEquals("ERROR", jsonResponse.get("status").asText(), "Expected status to be 'ERROR'");

        JsonNode alarms = jsonResponse.get("alarms");
        assertNotNull(alarms, "Alarms should not be null");
        assertTrue(alarms.isArray() && !alarms.isEmpty(), "Alarms should contain at least one error");

        JsonNode firstAlarm = alarms.get(0);
        assertEquals("AWS_CONNECTION_ERROR", firstAlarm.get("code").asText(), "Alarm code should be 'AWS_CONNECTION_ERROR'");
    }
    // spotless:on
}
