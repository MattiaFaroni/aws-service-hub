package com.aws.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoadBalancerStatusResponse {

    private StatusEnum status;
    private String timestamp;
    private Alarm alarm;

    @Getter
    @NoArgsConstructor
    public enum StatusEnum {
        ATTACHED("ATTACHED"),
        DETACHED("DETACHED");

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
    }
}
