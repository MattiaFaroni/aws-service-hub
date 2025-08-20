package com.aws.service.model;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ec2InstancesRequest {

    private String id;
    private String name;

    @NotNull
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
        public String toString() {
            return value;
        }

        @JsonbCreator
        public static StatusEnum fromValue(String value) {
            for (StatusEnum s : StatusEnum.values()) {
                if (s.value.equals(value)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }
}
