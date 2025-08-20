package com.aws.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alarm {

    private CodeEnum code;
    private String message;

    @Getter
    @NoArgsConstructor
    public enum CodeEnum {
        REQUEST_ERROR("REQUEST_ERROR"),
        AWS_CONNECTION_ERROR("AWS_CONNECTION_ERROR");

        private String value;

        CodeEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
