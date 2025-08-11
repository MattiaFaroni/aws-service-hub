package com.aws.service.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoadBalancerStatusRequest {

    @NotNull
    private String loadBalancerName;

    @NotNull
    private String instanceId;

    @NotNull
    private String region;

    @NotNull
    private Integer port;
}
