package com.aws.service.model;

import com.fasterxml.jackson.annotation.*;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
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

        public static CodeEnum fromString(String s) {
            for (CodeEnum b : CodeEnum.values()) {
                if (Objects.toString(b.value).equals(s)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected string value '" + s + "'");
        }

        @JsonCreator
        public static CodeEnum fromValue(String value) {
            for (CodeEnum b : CodeEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }
}
