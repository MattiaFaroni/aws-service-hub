package com.aws.service.api;

import com.aws.service.model.Ec2InstancesRequest;
import com.aws.service.model.Ec2InstancesResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;

public interface Ec2Interface {

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    Ec2InstancesResponse listEc2Instances(@Valid @NotNull Ec2InstancesRequest ec2InstancesRequest);
}
