import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.aws.service.service.HealthCheckService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.WireMockHelper;

public class HealthCheckTest {

    private HealthCheckService service;

    @BeforeEach
    void setup() {
        WireMockHelper.startServer();
        WireMockHelper.reset();

        System.setProperty("aws.region", "eu-central-1");
        System.setProperty("AWS_ACCESS_KEY_ID", "test");
        System.setProperty("AWS_SECRET_ACCESS_KEY", "test");
        System.setProperty("TARGET_GROUP_ARN", "arn:aws:elasticloadbalancing:123456789");

        String endpoint = "http://localhost:" + WireMockHelper.getPort();
        System.setProperty("software.amazon.awssdk.ec2.endpoint", endpoint);
        System.setProperty("software.amazon.awssdk.elasticloadbalancingv2.endpoint", endpoint);

        Cache<String, Boolean> noCache = Caffeine.newBuilder().maximumSize(0).build();
        service = new HealthCheckService(noCache);
    }

    @AfterEach
    void tearDown() {
        WireMockHelper.stopServer();
    }

    @Test
    void shouldReturnFalseWhenEc2Fails() {
        stubFor(post(urlPathEqualTo("/"))
                .withRequestBody(containing("DescribeInstances"))
                .willReturn(serverError()));

        stubFor(post(urlPathEqualTo("/"))
                .withRequestBody(containing("DescribeTargetHealth"))
                .willReturn(okXml("""
                            <DescribeTargetHealthResponse xmlns="http://elasticloadbalancing.amazonaws.com/doc/2015-12-01/">
                                <TargetHealthDescriptions/>
                            </DescribeTargetHealthResponse>
                        """)));

        boolean result = service.checkHealthCached();

        assertFalse(result, "Health check should fail if EC2 fails");
    }
}
