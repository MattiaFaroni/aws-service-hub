package com.aws.service.controller;

import com.aws.service.service.HealthCheckService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/healthcheck")
public class HealthCheckController {

    private final HealthCheckService healthCheckService = new HealthCheckService();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response health() {
        boolean healthy = healthCheckService.checkHealthCached();

        if (healthy) {
            return Response.ok("Services working").build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Health check failed")
                    .build();
        }
    }
}
