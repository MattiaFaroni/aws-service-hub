package com.aws.service.service;

import static com.aws.service.error.ErrorResponseBuilder.buildInternalServerErrorException;

import com.aws.service.model.Alarm;
import com.aws.service.model.LoadBalancerStatusRequest;
import com.aws.service.model.LoadBalancerStatusResponse;
import com.aws.service.tools.time.Timestamp;
import io.sentry.Sentry;
import jakarta.ws.rs.InternalServerErrorException;
import java.util.List;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.*;

public class LoadBalancerStatusService {

    // spotless:off
    public LoadBalancerStatusResponse checkInstanceStatus(LoadBalancerStatusRequest loadBalancerStatusRequest) {

        boolean connectedService = false;
        ElasticLoadBalancingV2Client loadBalancingClient = createLoadBalancerClient(loadBalancerStatusRequest.getRegion());

        if (loadBalancingClient != null) {
            String balancerArn = getLoadBalancerArn(loadBalancingClient, loadBalancerStatusRequest.getLoadBalancerName());
            if (balancerArn != null && !balancerArn.isEmpty()) {
                String targetGroupArn = getTargetGroupArn(loadBalancingClient, balancerArn, loadBalancerStatusRequest.getPort());
                if (targetGroupArn != null && !targetGroupArn.isEmpty()) {
                    connectedService = getInstanceStatus(loadBalancingClient, targetGroupArn, loadBalancerStatusRequest.getInstanceId());
                }
            }
            loadBalancingClient.close();
        }

        return generateResponse(connectedService);
    }

    /**
     * Creates an ElasticLoadBalancingV2Client configured for the specified AWS region.
     * @param region the AWS region for which the client should be configured
     * @return a configured ElasticLoadBalancingV2Client instance
     * @throws InternalServerErrorException if an error occurs while creating the client
     */
    private ElasticLoadBalancingV2Client createLoadBalancerClient(String region) {
        try {
            return ElasticLoadBalancingV2Client.builder().region(Region.of(region)).build();
        } catch (Exception e) {
            Sentry.captureException(e);
            throw buildInternalServerErrorException(e.getMessage(), Alarm.CodeEnum.AWS_CONNECTION_ERROR);
        }
    }

    /**
     * Retrieves the Amazon Resource Name (ARN) of the specified load balancer using the provided ElasticLoadBalancingV2Client.
     * @param loadBalancerClient the ElasticLoadBalancingV2Client to communicate with the AWS Elastic Load Balancing service
     * @param loadBalancerName the name of the load balancer for which to retrieve the ARN
     * @return the ARN of the specified load balancer, or null if the load balancer cannot be found
     * @throws InternalServerErrorException if an error occurs during the interaction with the AWS service
     */
    private String getLoadBalancerArn(ElasticLoadBalancingV2Client loadBalancerClient, String loadBalancerName) {
        try {
            DescribeLoadBalancersRequest request = DescribeLoadBalancersRequest.builder().names(loadBalancerName).build();
            DescribeLoadBalancersResponse response = loadBalancerClient.describeLoadBalancers(request);
            return response.loadBalancers().stream()
                    .findFirst()
                    .map(LoadBalancer::loadBalancerArn)
                    .orElse(null);

        } catch (Exception e) {
            Sentry.captureException(e);
            throw buildInternalServerErrorException(e.getMessage(), Alarm.CodeEnum.AWS_CONNECTION_ERROR);
        }
    }

    /**
     * Retrieves the Amazon Resource Name (ARN) of the target group associated with a specific port.
     * @param loadBalancing the ElasticLoadBalancingV2Client used to interact with the AWS Elastic Load Balancing service
     * @param balancerArn the ARN of the load balancer for which to retrieve the target group ARN
     * @param port the port associated with the target group
     * @return the ARN of the target group associated with the specified port, or null if no matching listener is found
     * @throws InternalServerErrorException if an error occurs during the interaction with the AWS service
     */
    private String getTargetGroupArn(ElasticLoadBalancingV2Client loadBalancing, String balancerArn, Integer port) {
        try {
            DescribeListenersRequest requestListener = DescribeListenersRequest.builder().loadBalancerArn(balancerArn).build();
            List<Listener> listeners = loadBalancing.describeListeners(requestListener).listeners();
            return listeners.stream()
                    .filter(listener -> listener.port().equals(port))
                    .findFirst()
                    .map(listener -> listener.defaultActions().getFirst().targetGroupArn())
                    .orElse(null);

        } catch (Exception e) {
            Sentry.captureException(e);
            throw buildInternalServerErrorException(e.getMessage(), Alarm.CodeEnum.AWS_CONNECTION_ERROR);
        }
    }

    /**
     * Checks if the specified instance is healthy within the given target group in AWS Elastic Load Balancing.
     * @param loadBalancing the ElasticLoadBalancingV2Client instance used to interact with the AWS Elastic Load Balancing service
     * @param targetGroupArn the Amazon Resource Name (ARN) of the target group to check
     * @param instanceId the ID of the instance to verify the health status for
     * @return true if the instance is healthy in the given target group, false otherwise
     * @throws InternalServerErrorException if an error occurs during the interaction with the AWS service
     */
    private boolean getInstanceStatus(ElasticLoadBalancingV2Client loadBalancing, String targetGroupArn, String instanceId) {
        try {
            DescribeTargetHealthRequest requestArn = DescribeTargetHealthRequest.builder().targetGroupArn(targetGroupArn).build();
            DescribeTargetHealthResponse responseArn = loadBalancing.describeTargetHealth(requestArn);
            return responseArn.targetHealthDescriptions().stream()
                    .anyMatch(target -> target.target().id().equals(instanceId) &&
                            target.targetHealth().stateAsString().equals("healthy"));

        } catch (Exception e) {
            Sentry.captureException(e);
            throw buildInternalServerErrorException(e.getMessage(), Alarm.CodeEnum.AWS_CONNECTION_ERROR);
        }
    }

    /**
     * Generates a response object representing the status of a load balancer connection.
     * @param connectedService indicates whether the load balancer is connected to a service.
     * @return object containing the load balancer connection status.
     */
    private LoadBalancerStatusResponse generateResponse(boolean connectedService) {
        LoadBalancerStatusResponse loadBalancerStatusResponse = new LoadBalancerStatusResponse();
        loadBalancerStatusResponse.setTimestamp(new Timestamp().toString());

        if (connectedService) {
            loadBalancerStatusResponse.setStatus(LoadBalancerStatusResponse.StatusEnum.ATTACHED);
        } else {
            loadBalancerStatusResponse.setStatus(LoadBalancerStatusResponse.StatusEnum.DETACHED);
        }

        return loadBalancerStatusResponse;
    }
    // spotless:on
}
