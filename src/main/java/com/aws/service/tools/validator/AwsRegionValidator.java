package com.aws.service.tools.validator;

import software.amazon.awssdk.regions.Region;

public class AwsRegionValidator {

    /**
     * Validates if the provided region string corresponds to a valid AWS region identifier.
     * @param regionStr the region string to validate
     * @return true if the provided region string matches a valid AWS region identifier, false otherwise
     */
    public static boolean isValidRegion(String regionStr) {
        if (regionStr == null || regionStr.isBlank()) {
            return false;
        }
        return Region.regions().stream().anyMatch(r -> r.id().equalsIgnoreCase(regionStr));
    }
}
