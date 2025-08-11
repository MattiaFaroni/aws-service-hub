package com.aws.service.model;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ec2InstancesResponse {

    private String timestamp;
    private Alarm alarm;
    private @Valid List<Instance> instances;
}
