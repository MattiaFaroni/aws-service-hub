package com.aws.service.service;

import static com.aws.service.error.ErrorResponseBuilder.buildBadRequest;

import com.aws.service.model.Alarm;
import com.aws.service.model.Ec2InstancesRequest;
import com.aws.service.model.Ec2InstancesResponse;
import com.aws.service.tools.validator.AwsRegionValidator;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

public class Ec2InstancesService {

    // spotless:off
    /**
     * Retrieves EC2 instances based on the provided request parameters.
     * @param request the request object containing filtering criteria
     * @return object that contains the list of EC2 instances matching the given criteria
     * @throws IllegalArgumentException if the specified AWS region is invalid.
     */
    public Ec2InstancesResponse getInstances(Ec2InstancesRequest request) {
        validateRegion(request.getRegion());

        try (Ec2Client ec2 = Ec2Client.builder().region(Region.of(request.getRegion())).build()) {
            List<Filter> filters = buildFiltersFromRequest(request);

            DescribeInstancesRequest awsRequest = DescribeInstancesRequest.builder().filters(filters).build();
            DescribeInstancesResponse awsResponse = ec2.describeInstances(awsRequest);

            return buildResponse(request, awsResponse);
        }
    }

    /**
     * Validates whether the provided AWS region is valid.
     * @param region the AWS region string to validate
     * @throws IllegalArgumentException if the specified AWS region is invalid
     */
    private void validateRegion(String region) {
        if (!AwsRegionValidator.isValidRegion(region)) {
            throw buildBadRequest("AWS region not valid", Alarm.CodeEnum.REQUEST_ERROR);
        }
    }

    /**
     * Builds a list of filters based on the given EC2 instance request parameters.
     * @param request the request object containing criteria for filtering EC2 instances
     * @return a list of filters derived from the request object
     */
    private List<Filter> buildFiltersFromRequest(Ec2InstancesRequest request) {
        List<Filter> filters = new ArrayList<>();

        addFilterIfNotNull(filters, "tag:Name", request.getName());
        addFilterIfNotNull(filters, "instance-state-name", request.getStatus() != null ? request.getStatus().toString().toLowerCase() : null);
        addFilterIfNotNull(filters, "instance-type", request.getType());
        addFilterIfNotNull(filters, "private-ip-address", request.getPrivateIp());
        addFilterIfNotNull(filters, "ip-address", request.getPublicIp());
        addFilterIfNotNull(filters, "subnet-id", request.getSubnetId());

        if ("windows".equalsIgnoreCase(request.getPlatform())) {
            filters.add(Filter.builder().name("platform").values("windows").build());
        }

        return filters;
    }

    /**
     * Adds a filter to the provided list if the specified value is not null.
     * @param filters the list of filters to which the new filter will be added
     * @param name the name of the filter to be added
     * @param value the value of the filter to be added
     */
    private void addFilterIfNotNull(List<Filter> filters, String name, String value) {
        if (value != null) {
            filters.add(Filter.builder().name(name).values(value).build());
        }
    }

    /**
     * Builds a response object containing filtered EC2 instances based on the given request
     * @param request the request object containing filtering criteria
     * @param awsResponse the response object returned by the AWS DescribeInstances API
     * @return an Ec2InstancesResponse object that encapsulates the list of filtered EC2 instances
     */
    private Ec2InstancesResponse buildResponse(Ec2InstancesRequest request, DescribeInstancesResponse awsResponse) {
        Ec2InstancesResponse response = new Ec2InstancesResponse();
        List<com.aws.service.model.Instance> instanceList = new ArrayList<>();

        for (Reservation reservation : awsResponse.reservations()) {
            for (Instance instance : reservation.instances()) {

                if (request.getId() != null && !instance.instanceId().equals(request.getId())) continue;
                if (request.getLaunchTime() != null
                        && !instance.launchTime()
                                .toString()
                                .startsWith(request.getLaunchTime().toString())) continue;

                instanceList.add(mapInstance(instance, request.getRegion()));
            }
        }

        response.setInstances(instanceList);
        return response;
    }

    /**
     * Maps an EC2 instance from the AWS SDK model to the application-specific model.
     * @param instance the AWS SDK instance to be mapped
     * @param region the AWS region where the instance is located
     * @return the application-specific instance model populated with the relevant data
     */
    private com.aws.service.model.Instance mapInstance(Instance instance, String region) {
        com.aws.service.model.Instance instanceModel = new com.aws.service.model.Instance();

        instanceModel.setId(instance.instanceId());

        instanceModel.setName(instance.tags().stream()
                .filter(t -> t.key().equals("Name"))
                .map(Tag::value)
                .findFirst()
                .orElse(null));

        instanceModel.setRegion(region);
        instanceModel.setStatus(com.aws.service.model.Instance.StatusEnum.fromString(instance.state().nameAsString().toUpperCase()));
        instanceModel.setType(instance.instanceTypeAsString());
        instanceModel.setPrivateIp(instance.privateIpAddress());
        instanceModel.setPublicIp(instance.publicIpAddress());
        instanceModel.setSubnetId(instance.subnetId());
        instanceModel.setPlatform(instance.platformAsString() == null ? "Linux/UNIX" : instance.platformAsString());

        instanceModel.setLaunchTime(instance.launchTime().atOffset(ZoneOffset.UTC));

        return instanceModel;
    }
    // spotless:on
}
