package com.aws.service.service;

import com.aws.service.model.Alarm;
import com.aws.service.model.LoadBalancerStatusRequest;
import com.aws.service.model.LoadBalancerStatusResponse;
import com.aws.service.tools.Timestamp;
import io.sentry.Sentry;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.*;

public class LoadBalancerStatusService {

    ArrayList<Alarm> alarmList = new ArrayList<>();

    /**
     * Method used to check whether a service is connected to the load balancer or not
     * @param loadBalancerStatusRequest service request
     * @return service response
     */
    // spotless:off
    public LoadBalancerStatusResponse checkInstanceStatus(LoadBalancerStatusRequest loadBalancerStatusRequest) {

        boolean connectedService = false;
        ElasticLoadBalancingV2Client loadBalancingClient =
                createLoadBalancerClient(loadBalancerStatusRequest.getRegion());

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
     * Method used to create a new aws load balancer client
     * @param region name of the region
     * @return aws load balancer client
     */
    private ElasticLoadBalancingV2Client createLoadBalancerClient(String region) {
        try {
            return ElasticLoadBalancingV2Client.builder().region(Region.of(region)).build();

        } catch (Exception e) {
            Sentry.captureException(e);
            generateAlarm(e.getMessage());
            return null;
        }
    }

    /**
     * Method used to get aws load balancer arn
     * @param loadBalancerClient aws load balancer client
     * @param loadBalancerName load balancer name
     * @return load balancer arn
     */
    private String getLoadBalancerArn(ElasticLoadBalancingV2Client loadBalancerClient, String loadBalancerName) {
        String balancerArn = null;
        DescribeLoadBalancersRequest request = DescribeLoadBalancersRequest.builder().names(loadBalancerName).build();

        try {
            DescribeLoadBalancersResponse response = loadBalancerClient.describeLoadBalancers(request);
            for (LoadBalancer loadBalancer : response.loadBalancers()) {
                balancerArn = loadBalancer.loadBalancerArn();
                break;
            }

        } catch (Exception e) {
            Sentry.captureException(e);
            generateAlarm(e.getMessage());
            return null;
        }
        return balancerArn;
    }

    /**
     * Method used to get aws target group arn
     * @param loadBalancing aws load balancer client
     * @param balancerArn aws load balancer arn
     * @param port service port
     * @return aws target group arn
     */
    private String getTargetGroupArn(ElasticLoadBalancingV2Client loadBalancing, String balancerArn, Integer port) {
        String targetGroupArn = null;

        try {
            DescribeListenersRequest requestListener = DescribeListenersRequest.builder().loadBalancerArn(balancerArn).build();
            List<Listener> listeners = loadBalancing.describeListeners(requestListener).listeners();

            for (Listener listener : listeners) {
                if (listener.port().equals(port)) {
                    targetGroupArn = listener.defaultActions().getFirst().targetGroupArn();
                    break;
                }
            }

        } catch (Exception e) {
            Sentry.captureException(e);
            generateAlarm(e.getMessage());
            return null;
        }

        return targetGroupArn;
    }

    /**
     * Method used to check whether a service is connected to the load balancer or not
     * @param loadBalancing aws load balancer client
     * @param targetGroupArn aws target group arn
     * @param instanceId instance id
     * @return service status
     */
    private boolean getInstanceStatus(ElasticLoadBalancingV2Client loadBalancing, String targetGroupArn, String instanceId) {
        try {
            DescribeTargetHealthRequest requestArn = DescribeTargetHealthRequest.builder().targetGroupArn(targetGroupArn).build();
            DescribeTargetHealthResponse responseArn = loadBalancing.describeTargetHealth(requestArn);

            for (TargetHealthDescription target : responseArn.targetHealthDescriptions()) {
                String id = target.target().id();
                String status = target.targetHealth().stateAsString();
                if (instanceId.equals(id)) {
                    if (status.equals("healthy")) {
                        return true;
                    }
                }
            }
            return false;

        } catch (Exception e) {
            Sentry.captureException(e);
            generateAlarm(e.getMessage());
            return false;
        }
    }

    /**
     * Method used to generate the service response
     * @param connectedService flag whether the service is connected to the load balancer or not
     * @return service response
     */
    private LoadBalancerStatusResponse generateResponse(boolean connectedService) {
        LoadBalancerStatusResponse loadBalancerStatusResponse = new LoadBalancerStatusResponse();
        loadBalancerStatusResponse.setTimestamp(new Timestamp().toString());

        if (alarmList.isEmpty()) {
            if (connectedService) {
                loadBalancerStatusResponse.setStatus(LoadBalancerStatusResponse.StatusEnum.ATTACHED);
            } else {
                loadBalancerStatusResponse.setStatus(LoadBalancerStatusResponse.StatusEnum.DETACHED);
            }

        } else {
            loadBalancerStatusResponse.setStatus(LoadBalancerStatusResponse.StatusEnum.ERROR);
            loadBalancerStatusResponse.setAlarms(alarmList);
            throw new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(loadBalancerStatusResponse)
                    .build());
        }

        return loadBalancerStatusResponse;
    }
    // spotless:on

    /**
     * Method used to generate a new alarm
     * @param exception exception message
     */
    private void generateAlarm(String exception) {
        Alarm alarm = new Alarm();
        alarm.setCode(Alarm.CodeEnum.AWS_CONNECTION_ERROR);
        alarm.setMessage(exception);
        alarmList.add(alarm);
    }
}
