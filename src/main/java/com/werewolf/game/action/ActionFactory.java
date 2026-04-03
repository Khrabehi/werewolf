package com.werewolf.game.action;

public class ActionFactory {
    
    public GameAction getAction(String actionType) {
        switch (actionType.toUpperCase()) {
            case "KILL":
                return new KillAction();
            case "VOTE":
                // return new VoteAction();
            case "PROTECT":
                // return new ProtectAction();
            default:
                throw new IllegalArgumentException("Unknown action type: " + actionType);
        }
    }
}