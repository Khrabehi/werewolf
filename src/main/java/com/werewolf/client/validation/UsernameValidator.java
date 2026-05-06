package com.werewolf.client.validation;

import com.werewolf.validation.ValidationResult;

public class UsernameValidator implements ClientValidator<String> {
    private static final int MAX_LENGTH = 16;
    private static final String FORBIDDEN_CHARS = "[<>:\"\\|?*]";

    @Override
    public ValidationResult validate(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.INVALID("Username cannot be empty");
        }

        if (username.length() > MAX_LENGTH) {
            return ValidationResult.INVALID(
                "Username cannot exceed " + MAX_LENGTH + " characters"
            );
        }

        if (username.matches(".*" + FORBIDDEN_CHARS + ".*")) {
            return ValidationResult.INVALID(
                "Username contains forbidden characters: < > : \" \\ | ? *"
            );
        }

        if (!username.matches("[a-zA-Z0-9_-]+")) {
            return ValidationResult.INVALID(
                "Username can only contain letters, numbers, underscores, and hyphens"
            );
        }

        return ValidationResult.VALID();
    }
}
