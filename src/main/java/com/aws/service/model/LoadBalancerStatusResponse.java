package com.aws.service.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
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
public class LoadBalancerStatusResponse {

    private StatusEnum status;
    private String timestamp;
    private @Valid List<@Valid Alarm> alarms;

    @Getter
    @NoArgsConstructor
    public enum StatusEnum {
        ATTACHED("attached"),
        DETACHED("detached"),
        ERROR("error");

        private String value;

        StatusEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        public static StatusEnum fromString(String s) {
            for (StatusEnum b : StatusEnum.values()) {
                if (Objects.toString(b.value).equals(s)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected string value '" + s + "'");
        }

        @JsonCreator
        public static StatusEnum fromValue(String value) {
            for (StatusEnum b : StatusEnum.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }
}
