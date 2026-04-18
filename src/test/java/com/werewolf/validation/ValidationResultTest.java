package com.werewolf.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationResult Tests")
public class ValidationResultTest {
    
    @Test
    @DisplayName("ValidationResult.VALID() creates valid result")
    public void testValidationResultValid() {
        ValidationResult result = ValidationResult.VALID();
        assertTrue(result.isValid());
    }
    
    @Test
    @DisplayName("ValidationResult.INVALID() creates invalid result")
    public void testValidationResultInvalid() {
        ValidationResult result = ValidationResult.INVALID("Player does not exist");
        assertFalse(result.isValid());
    }
    
    @Test
    @DisplayName("ValidationResult stores error message")
    public void testValidationResultMessage() {
        String reason = "Player is dead";
        ValidationResult result = ValidationResult.INVALID(reason);
        
        assertFalse(result.isValid());
        assertEquals(reason, result.getErrorMessage());
    }
    
    @Test
    @DisplayName("ValidationResult valid has null message")
    public void testValidResultMessage() {
        ValidationResult result = ValidationResult.VALID();
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    @DisplayName("Different ValidationResults are independent")
    public void testMultipleResults() {
        ValidationResult result1 = ValidationResult.VALID();
        ValidationResult result2 = ValidationResult.INVALID("Error");
        
        assertTrue(result1.isValid());
        assertFalse(result2.isValid());
    }
    
    @Test
    @DisplayName("ValidationResult can be used in if statements")
    public void testValidationFlow() {
        ValidationResult result = ValidationResult.VALID();
        
        if (result.isValid()) {
            assertTrue(true);
        } else {
            fail("Should be valid");
        }
    }
    
    @Test
    @DisplayName("CommandExecutionResult success")
    public void testCommandExecutionResultSuccess() {
        CommandExecutionResult result = CommandExecutionResult.success();
        assertTrue(result.isSuccess());
    }
    
    @Test
    @DisplayName("CommandExecutionResult failed")
    public void testCommandExecutionResultFailed() {
        String errorMsg = "Action failed";
        CommandExecutionResult result = CommandExecutionResult.failed(errorMsg);
        
        assertFalse(result.isSuccess());
        assertEquals(errorMsg, result.getErrorMessage());
    }
    
    @Test
    @DisplayName("CommandExecutionResult success has message")
    public void testCommandExecutionResultSuccessMessage() {
        CommandExecutionResult result = CommandExecutionResult.success();
        assertTrue(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test
    @DisplayName("Multiple CommandExecutionResults are independent")
    public void testMultipleCommandResults() {
        CommandExecutionResult success = CommandExecutionResult.success();
        CommandExecutionResult failure = CommandExecutionResult.failed("Error occurred");
        
        assertTrue(success.isSuccess());
        assertFalse(failure.isSuccess());
    }
    
    @Test
    @DisplayName("Validation passed but execution failed")
    public void testValidationAndExecutionFlow() {
        // Validation passed
        ValidationResult validation = ValidationResult.VALID();
        assertTrue(validation.isValid());
        
        // But execution failed
        CommandExecutionResult execution = CommandExecutionResult.failed("Concurrent modification");
        assertFalse(execution.isSuccess());
    }
    
    @Test
    @DisplayName("ValidationResult with special characters")
    public void testSpecialCharactersMessage() {
        String message = "Error: @#$%^&*()_+";
        ValidationResult result = ValidationResult.INVALID(message);
        
        assertEquals(message, result.getErrorMessage());
    }
    
    @Test
    @DisplayName("CommandExecutionResult with empty message")
    public void testEmptyErrorMessage() {
        CommandExecutionResult result = CommandExecutionResult.failed("");
        assertFalse(result.isSuccess());
        assertEquals("", result.getErrorMessage());
    }
}
