package com.werewolf.client.validation;

import com.werewolf.validation.ValidationResult;

public class PortValidator implements ClientValidator<String> {
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    @Override
    public ValidationResult validate(String port) {
        if (port == null || port.trim().isEmpty()) {
            return ValidationResult.INVALID("Port cannot be empty");
        }

        try {
            int portNumber = Integer.parseInt(port.trim());

            if (portNumber < MIN_PORT || portNumber > MAX_PORT) {
                return ValidationResult.INVALID(
                    "Port must be between " + MIN_PORT + " and " + MAX_PORT
                );
            }

            return ValidationResult.VALID();
        } catch (NumberFormatException e) {
            return ValidationResult.INVALID("Port must be a valid number");
        }
    }
}
