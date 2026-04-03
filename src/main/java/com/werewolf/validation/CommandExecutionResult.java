package com.werewolf.validation;

public class CommandExecutionResult {
    private final boolean success;
    private final String errorMessage;

    private CommandExecutionResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static CommandExecutionResult success() {
        return new CommandExecutionResult(true, "Command executed successfully");
    }

    public static CommandExecutionResult failed(String errorMessage) {
        return new CommandExecutionResult(false, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}