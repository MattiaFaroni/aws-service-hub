package com.aws.service.error;

import com.aws.service.model.Alarm;
import com.aws.service.model.BaseAlarmResponse;
import com.aws.service.tools.time.Timestamp;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Response;

public class ErrorResponseBuilder {

    /**
     * Constructs a BadRequestException containing an error response.
     * @param message the error message to be included in the error response
     * @param code the specific error code of type Alarm.CodeEnum indicating the nature of the error
     * @return an instance of BadRequestException containing the error response
     */
    // spotless:off
    public static BadRequestException buildBadRequest(String message, Alarm.CodeEnum code) {
        BaseAlarmResponse errorResponse = buildErrorResponse(message, code);
        return new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build());
    }

    /**
     * Builds an InternalServerErrorException containing an error response.
     * @param message the error message to be included in the error response
     * @param code the specific error code of type Alarm.CodeEnum indicating the nature of the error
     * @return an instance of InternalServerErrorException containing the error response
     */
    public static InternalServerErrorException buildInternalServerErrorException(String message, Alarm.CodeEnum code) {
        BaseAlarmResponse errorResponse = buildErrorResponse(message, code);
        return new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build());
    }

    /**
     * Builds an error response.
     * @param message the error message to be included in the response
     * @param code the specific error code of type Alarm.CodeEnum indicating the nature of the error
     * @return an instance of BaseAlarmResponse containing the error details
     */
    public static BaseAlarmResponse buildErrorResponse(String message, Alarm.CodeEnum code) {
        BaseAlarmResponse errorResponse = new BaseAlarmResponse();

        Alarm alarm = new Alarm();
        alarm.setCode(code);
        alarm.setMessage(message);

        errorResponse.setTimestamp(new Timestamp().toString());
        errorResponse.setAlarm(alarm);

        return errorResponse;
    }
    // spotless:on
}
