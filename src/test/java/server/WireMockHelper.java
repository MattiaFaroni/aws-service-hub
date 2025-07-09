package server;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

import com.github.tomakehurst.wiremock.WireMockServer;

public class WireMockHelper {

    private static final int PORT = 8080;
    private static WireMockServer wireMockServer;

    /**
     * Starts the WireMock server to enable mocking of HTTP requests and responses.
     */
    public static void startServer() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(PORT);
            wireMockServer.start();
            configureFor("localhost", PORT);
        }
    }

    /**
     * Stops the WireMock server if it is currently running.
     */
    public static void stopServer() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    /**
     * Retrieves the port number on which the WireMock server is configured to run.
     * @return the port number used by the WireMock server
     */
    public static int getPort() {
        return PORT;
    }

    /**
     * Resets the state of the WireMock server by removing all mappings, requests, and scenarios.
     */
    public static void reset() {
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
    }
}
