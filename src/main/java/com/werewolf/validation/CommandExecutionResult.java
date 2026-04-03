package com.werewolf.validation;

public class CommandExecutionResult {
    private final boolean success;
    private final String message;

    private CommandExecutionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
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

    public String getMessage() {
        return message;
    }
}