package com.aws.service.model;

import com.fasterxml.jackson.annotation.*;
import java.time.OffsetDateTime;
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
public class Instance {

    private String id;
    private String name;
    private String region;
    private StatusEnum status;
    private String type;
    private String privateIp;
    private String publicIp;
    private String subnetId;
    private String platform;
    private OffsetDateTime launchTime;

    @Getter
    @NoArgsConstructor
    public enum StatusEnum {
        PENDING("PENDING"),
        RUNNING("RUNNING"),
        SHUTTING_DOWN("SHUTTING-DOWN"),
        TERMINATED("TERMINATED"),
        STOPPING("STOPPING"),
        STOPPED("STOPPED");

        private String value;

        StatusEnum(String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
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
