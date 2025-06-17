package com.aws.service.controller;

import static com.aws.service.error.ErrorResponseBuilder.buildBadRequest;

import com.aws.service.api.LoadBalancerInterface;
import com.aws.service.model.Alarm;
import com.aws.service.model.LoadBalancerStatusRequest;
import com.aws.service.model.LoadBalancerStatusResponse;
import com.aws.service.service.LoadBalancerStatusService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Path;
import java.util.Set;

@Path("/load-balancer/service/status")
public class LoadBalancerController implements LoadBalancerInterface {

    // spotless:off
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();
    private final LoadBalancerStatusService loadBalancerStatusService = new LoadBalancerStatusService();

    @Override
    public LoadBalancerStatusResponse loadBalancerInstance(LoadBalancerStatusRequest request) {

        if (request == null) {
            throw buildBadRequest("Request body not valid", Alarm.CodeEnum.REQUEST_ERROR);
        }

        Set<ConstraintViolation<LoadBalancerStatusRequest>> constraintViolations = validator.validate(request);
        if (!constraintViolations.isEmpty()) {
            throw buildValidationError(constraintViolations);
        }

        return loadBalancerStatusService.checkInstanceStatus(request);
    }

    /**
     * Constructs a BadRequestException using the validation errors provided in the set of constraint violations.
     * @param violations a set of constraint violations that occurred during validation of the request
     * @return a BadRequestException containing an error message representing the validation issue
     */
    private BadRequestException buildValidationError(Set<ConstraintViolation<LoadBalancerStatusRequest>> violations) {
        String error = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Invalid request");
        return buildBadRequest(error, Alarm.CodeEnum.REQUEST_ERROR);
    }
    // spotless:on
}
