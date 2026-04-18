// Fichier : src/main/java/com/werewolf/validation/ValidationResult.java
package com.werewolf.validation;

public class ValidationResult {
    private boolean valid;
    private String errorMessage;
    
    private ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }
    

    public static ValidationResult VALID() {
        return new ValidationResult(true, null);
    }
    
    public static ValidationResult INVALID(String reason) {
        return new ValidationResult(false, reason);
    }
        
    public boolean isValid() { 
        return valid; 
    }
    
    public String getErrorMessage() { 
        return errorMessage; 
    }
}