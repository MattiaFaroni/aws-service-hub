package com.aws.service.controller;

import static com.aws.service.error.ErrorResponseBuilder.buildBadRequest;

import com.aws.service.api.Ec2Interface;
import com.aws.service.model.*;
import com.aws.service.service.Ec2InstancesService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Path;
import java.util.Set;

@Path("/ec2/instances")
public class Ec2InstanceController implements Ec2Interface {

    // spotless:off
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private final Ec2InstancesService ec2InstancesService = new Ec2InstancesService();

    @Override
    public Ec2InstancesResponse listEc2Instances(Ec2InstancesRequest request) {

        if (request == null) {
            throw buildBadRequest("Request body not valid", Alarm.CodeEnum.REQUEST_ERROR);
        }

        Set<ConstraintViolation<Ec2InstancesRequest>> constraintViolations = validator.validate(request);
        if (!constraintViolations.isEmpty()) {
            throw buildValidationError(constraintViolations);
        }

        return ec2InstancesService.getInstances(request);
    }

    /**
     * Constructs a BadRequestException using the validation errors provided in the set of constraint violations.
     * @param violations a set of constraint violations that occurred during validation of the request
     * @return a BadRequestException containing an error message representing the validation issue
     */
    private BadRequestException buildValidationError(Set<ConstraintViolation<Ec2InstancesRequest>> violations) {
        String error = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Invalid request");
        return buildBadRequest(error, Alarm.CodeEnum.REQUEST_ERROR);
    }
    // spotless:on
}
