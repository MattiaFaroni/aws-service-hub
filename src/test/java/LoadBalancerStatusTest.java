import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import server.WireMockHelper;

public class LoadBalancerStatusTest {

    private static final String BASE_URL =
            "http://localhost:" + WireMockHelper.getPort() + "/aws/load-balancer/service/status";
    private static final String CONTENT_TYPE = "application/json";

    private HttpClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        WireMockHelper.startServer();
        WireMockHelper.reset();

        client = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        WireMockHelper.stopServer();
    }

    /**
     * Sends an HTTP POST request.
     * @param body the request body to be sent as the payload of the POST request
     * @return the HTTP response received after sending the request
     * @throws IOException if an I/O error occurs when sending or receiving data
     * @throws InterruptedException if the operation is interrupted while waiting for a response
     */
    private HttpResponse<String> sendPostRequest(String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", CONTENT_TYPE)
                .header("Accept", CONTENT_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Parses a JSON-formatted string into a JsonNode object.
     * @param body the JSON string to parse
     * @return a JsonNode representation of the input JSON string
     * @throws IOException if an error occurs while reading or parsing the JSON
     */
    private JsonNode parseJson(String body) throws IOException {
        return objectMapper.readTree(body);
    }

    /**
     * Validates the structure and content of the provided alarms JSON node.
     * @param alarms the JSON node representing the alarms to validate
     * @param expectedCode the expected code of the first alarm
     */
    private void assertAlarm(JsonNode alarms, String expectedCode) {
        assertNotNull(alarms, "Alarms should not be null");
        assertTrue(alarms.isArray() && !alarms.isEmpty(), "Alarms should contain at least one error");
        assertEquals(expectedCode, alarms.get(0).get("code").asText(), "Unexpected alarm code");
    }

    @Nested
    class InvalidRequestTests {

        @Test
        void requestBodyEmpty() throws Exception {

            stubFor(post(urlEqualTo("/aws/load-balancer/service/status"))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", CONTENT_TYPE)
                            .withBody("""
                                        {
                                          "status": "ERROR",
                                          "alarms": [
                                            { "code": "REQUEST_ERROR" }
                                          ]
                                        }
                                    """)));

            HttpResponse<String> response = sendPostRequest("{}");

            assertEquals(400, response.statusCode(), "Expected HTTP status 400");
            JsonNode jsonResponse = parseJson(response.body());
            assertEquals("ERROR", jsonResponse.get("status").asText(), "Expected status to be 'ERROR'");
            assertAlarm(jsonResponse.get("alarms"), "REQUEST_ERROR");
        }

        @Test
        void requestBodyIncorrect() throws Exception {

            String requestBody = """
                    {
                      "loadBalancerName": "123456789",
                      "instanceId": "123456789",
                      "region": "123456789",
                      "port": "123456789"
                    }
                    """;

            stubFor(post(urlEqualTo("/aws/load-balancer/service/status"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withHeader("Content-Type", CONTENT_TYPE)
                            .withBody("""
                                        {
                                          "status": "ERROR",
                                          "alarms": [
                                            { "code": "AWS_CONNECTION_ERROR" }
                                          ]
                                        }
                                    """)));

            HttpResponse<String> response = sendPostRequest(requestBody);

            assertEquals(500, response.statusCode(), "Expected HTTP status 500");
            JsonNode jsonResponse = parseJson(response.body());
            assertEquals("ERROR", jsonResponse.get("status").asText(), "Expected status to be 'ERROR'");
            assertAlarm(jsonResponse.get("alarms"), "AWS_CONNECTION_ERROR");
        }
    }
}
