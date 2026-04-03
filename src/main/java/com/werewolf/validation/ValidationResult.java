package com.werewolf.validation;

public class ValidationResult {
    private final boolean isValid;
    private final String errorMessage;

    private ValidationResult(boolean isValid, String errorMessage) {
        this.isValid = isValid;
        this.errorMessage = errorMessage;
    }

    public static ValidationResult VALID() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult INVALID(String message) {
        return new ValidationResult(false, message);
    }

    public boolean isValid() {
        return isValid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
