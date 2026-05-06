package com.werewolf.client.validation;

import com.werewolf.validation.ValidationResult;

public class IpAddressValidator implements ClientValidator<String> {
    private static final String IPV4_PATTERN =
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final String HOSTNAME_PATTERN =
        "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*" +
        "([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])$";

    @Override
    public ValidationResult validate(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return ValidationResult.INVALID("IP Address cannot be empty");
        }

        String ip = ipAddress.trim();

        if (!ip.matches(IPV4_PATTERN) && !ip.matches(HOSTNAME_PATTERN) && !ip.equals("localhost")) {
            return ValidationResult.INVALID(
                "Invalid IP address or hostname format"
            );
        }

        return ValidationResult.VALID();
    }
}
