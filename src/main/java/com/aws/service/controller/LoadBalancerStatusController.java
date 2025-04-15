package com.aws.service.controller;

import com.aws.service.api.LoadBalancerStatusInterface;
import com.aws.service.model.Alarm;
import com.aws.service.model.LoadBalancerStatusRequest;
import com.aws.service.model.LoadBalancerStatusResponse;
import com.aws.service.service.LoadBalancerStatusService;
import com.aws.service.tools.Timestamp;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

@Path("/load-balancer/service/status")
public class LoadBalancerStatusController implements LoadBalancerStatusInterface {

    // spotless:off
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    @Override
    public LoadBalancerStatusResponse loadBalancerInstance(LoadBalancerStatusRequest loadBalancerStatusRequest) {

        if (loadBalancerStatusRequest == null) {
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(generateErrorResponse("request body not valid"))
                    .build());
        }

        Set<ConstraintViolation<LoadBalancerStatusRequest>> constraintViolations = validator.validate(loadBalancerStatusRequest);

        if (constraintViolations.isEmpty()) {
            LoadBalancerStatusService loadBalancerStatusService = new LoadBalancerStatusService();
            return loadBalancerStatusService.checkInstanceStatus(loadBalancerStatusRequest);

        } else {
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(generateParametersException(constraintViolations.stream().findFirst().get().getPropertyPath()))
                    .build());
        }
    }

    /**
     * Method used to return an error in case of invalid parameters
     * @param propertyPath variables in error
     * @return service response
     */
    private LoadBalancerStatusResponse generateParametersException(jakarta.validation.Path propertyPath) {
        Iterator<jakarta.validation.Path.Node> iterator = propertyPath.iterator();
        String errorDescription = "";

        while (iterator.hasNext()) {
            String name = String.valueOf(iterator.next());
            if (!iterator.hasNext()) {
                errorDescription = name + " parameter not found";
            }
        }

        return generateErrorResponse(errorDescription);
    }

    /**
     * Method used to generate the service response in case of error
     * @param errorMessage error message
     * @return service response
     */
    private LoadBalancerStatusResponse generateErrorResponse(String errorMessage) {
        LoadBalancerStatusResponse loadBalancerStatusResponse = new LoadBalancerStatusResponse();
        loadBalancerStatusResponse.setStatus(LoadBalancerStatusResponse.StatusEnum.ERROR);
        loadBalancerStatusResponse.setTimestamp(new Timestamp().toString());

        ArrayList<Alarm> alarmList = new ArrayList<>();
        Alarm alarm = new Alarm();
        alarm.setCode(Alarm.CodeEnum.REQUEST_ERROR);
        alarm.setMessage(errorMessage);
        alarmList.add(alarm);

        loadBalancerStatusResponse.setAlarms(alarmList);
        return loadBalancerStatusResponse;
    }
    // spotless:on
}
