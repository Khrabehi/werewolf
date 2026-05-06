package com.werewolf.game.action;

public class ActionFactory {

    public GameAction getAction(String actionType) {
        if (actionType == null || actionType.trim().isEmpty()) {
            throw new IllegalArgumentException("Action type must not be null or blank");
        }

        String normalizedActionType = actionType.trim().toUpperCase();
        return switch (normalizedActionType) {
            case "KILL" -> new KillAction();
            case "VOTE" -> new VoteAction();
            case "HEAL" -> new ProtectAction();
            case "PEEK" -> new InvestigateAction();
            default -> throw new IllegalArgumentException("Unknown action: " + actionType);
        };
    }
}