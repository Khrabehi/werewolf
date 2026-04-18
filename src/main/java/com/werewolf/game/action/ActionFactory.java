package com.werewolf.game.action;

import java.util.Map;

public class ActionFactory {
    private static final Map<String, GameAction> ACTIONS = Map.ofEntries(
        Map.entry("KILL", new KillAction()),
        Map.entry("VOTE", new VoteAction()),
        Map.entry("HEAL", new ProtectAction()),
        Map.entry("PEEK", new InvestigateAction())
    );
    
    public GameAction getAction(String actionType) {
    return switch(actionType.toUpperCase()) {
        case "KILL" -> new KillAction();
        case "VOTE" -> new VoteAction();
        case "HEAL" -> new ProtectAction();
        case "PEEK" -> new InvestigateAction();
        default -> throw new IllegalArgumentException("Unknown action: " + actionType);
    };
}
}