package com.aws.service.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.sentry.Sentry;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthResponse;

public class HealthCheckService {

    private final Cache<String, Boolean> healthCache;
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);

    // spotless:off
    public HealthCheckService() {
        healthCache = Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();
    }
    //spotless:on

    public HealthCheckService(Cache<String, Boolean> cache) {
        this.healthCache = cache;
    }

    /**
     * Checks the cached health status of AWS services.
     * @return true if both EC2 and ELB health checks pass, false otherwise.
     */
    public boolean checkHealthCached() {
        return Boolean.TRUE.equals(healthCache.get("aws-health", key -> checkEc2() && checkElb()));
    }

    /**
     * Checks the health of the EC2 service by attempting to describe instances.
     * @return true if the EC2 service is accessible and returns a valid response.
     */
    public boolean checkEc2() {
        try (Ec2Client ec2 = Ec2Client.create()) {
            DescribeInstancesResponse response = ec2.describeInstances(
                    DescribeInstancesRequest.builder().maxResults(5).build());

            if (response.reservations() == null || response.reservations().isEmpty()) {
                logger.error("EC2 check failed: no reservations found");
                return false;
            }
            return true;

        } catch (Exception e) {
            Sentry.captureException(e);
            logger.error("EC2 check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies the status of the Elastic Load Balancer (ELB) by checking the health of its target group.
     * @return true if the ELB's target group health details are successfully retrieved.
     */
    public boolean checkElb() {
        String targetGroupArn = System.getenv("TARGET_GROUP_ARN");
        if (targetGroupArn == null || targetGroupArn.isBlank()) {
            logger.warn("ELB check skipped: no TARGET_GROUP_ARN set");
            return true;
        }

        try (ElasticLoadBalancingV2Client elb = ElasticLoadBalancingV2Client.create()) {
            DescribeTargetHealthResponse response = elb.describeTargetHealth(DescribeTargetHealthRequest.builder()
                    .targetGroupArn(targetGroupArn)
                    .build());
            return response.targetHealthDescriptions() != null;

        } catch (Exception e) {
            Sentry.captureException(e);
            logger.error("ELB check failed: {}", e.getMessage());
            return false;
        }
    }
}
