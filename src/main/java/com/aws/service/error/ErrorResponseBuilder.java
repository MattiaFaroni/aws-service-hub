package com.aws.service.error;

import com.aws.service.model.Alarm;
import com.aws.service.model.LoadBalancerStatusResponse;
import com.aws.service.tools.Timestamp;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;

public class ErrorResponseBuilder {

    /**
     * Builds and returns a BadRequestException with a specified error message and error code.
     * @param message the error message to include in the response
     * @param code the specific error code to include in the response
     * @return a BadRequestException containing the error response with the specified message and code
     */
    public static BadRequestException buildBadRequest(String message, Alarm.CodeEnum code) {
        LoadBalancerStatusResponse errorResponse = buildErrorResponse(message, code);
        return new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build());
    }

    /**
     * Builds and returns an InternalServerErrorException with a specified error message and error code.
     * @param message the error message to include in the response
     * @param code the specific error code to include in the alarm of the response
     * @return an InternalServerErrorException instance containing the error response with the specified message and code
     */
    public static InternalServerErrorException buildInternalServerErrorException(String message, Alarm.CodeEnum code) {
        LoadBalancerStatusResponse errorResponse = buildErrorResponse(message, code);
        return new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build());
    }

    /**
     * Builds an error response for a load balancer operation.
     * @param message the error message to include in the response
     * @param code the specific error code to include in the alarm of the response
     * @return a LoadBalancerStatusResponse object containing the error status, timestamp, and alarm details
     */
    private static LoadBalancerStatusResponse buildErrorResponse(String message, Alarm.CodeEnum code) {
        LoadBalancerStatusResponse response = new LoadBalancerStatusResponse();
        response.setStatus(LoadBalancerStatusResponse.StatusEnum.ERROR);
        response.setTimestamp(new Timestamp().toString());

        Alarm alarm = new Alarm();
        alarm.setCode(code);
        alarm.setMessage(message);

        response.setAlarm(alarm);
        return response;
    }
}
