package com.aws.service.api;

import com.aws.service.model.LoadBalancerStatusRequest;
import com.aws.service.model.LoadBalancerStatusResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;

public interface LoadBalancerStatusInterface {

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    LoadBalancerStatusResponse loadBalancerInstance(@Valid LoadBalancerStatusRequest loadBalancerStatusRequest);
}
