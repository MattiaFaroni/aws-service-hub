package com.aws.service.error;

import com.aws.service.model.Alarm;
import com.aws.service.model.LoadBalancerStatusResponse;
import com.aws.service.tools.Timestamp;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;

public class ErrorResponseBuilder {

    /**
     * Constructs a BadRequestException containing an error response.
     * @param <T> the type of the error response entity
     * @param message the error message to be included in the error response
     * @param code the specific error code of type Alarm.CodeEnum indicating the nature of the error
     * @param responseType the class type of the error response entity to be constructed
     * @return a BadRequestException containing the error response
     */
    // spotless:off
    public static <T> BadRequestException buildBadRequest(String message, Alarm.CodeEnum code, Class<T> responseType) {
        T errorResponse = buildErrorResponse(message, code, responseType);
        return new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build());
    }

    /**
     * Builds an InternalServerErrorException containing an error response.
     * @param <T> the type of the error response entity
     * @param message the error message to be included in the error response
     * @param code the specific error code of type Alarm.CodeEnum indicating the nature of the error
     * @param responseType the class type of the error response entity to be constructed
     * @return an InternalServerErrorException containing the error response
     */
    public static <T> InternalServerErrorException buildInternalServerErrorException(String message, Alarm.CodeEnum code, Class<T> responseType) {
        T errorResponse = buildErrorResponse(message, code, responseType);
        return new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build());
    }

    /**
     * Builds an error response object of the specified type.
     * @param <T> the type of the error response to be constructed
     * @param message the error message to be included in the response
     * @param code the specific error code of type {@link Alarm.CodeEnum} indicating the nature of the error
     * @param responseType the class type of the response object to be created
     * @return an instance of the specified response type containing the error details
     * @throws RuntimeException if an instance of the specified response type cannot be created
     */
    public static <T> T buildErrorResponse(String message, Alarm.CodeEnum code, Class<T> responseType) {
        try {
            T response = responseType.getConstructor().newInstance();

            Alarm alarm = new Alarm();
            alarm.setCode(code);
            alarm.setMessage(message);

            if (response instanceof LoadBalancerStatusResponse lbResponse) {
                lbResponse.setStatus(LoadBalancerStatusResponse.StatusEnum.ERROR);
                lbResponse.setTimestamp(new Timestamp().toString());
                lbResponse.setAlarm(alarm);
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error creating response. ", e);
        }
    }
    // spotless:on
}
