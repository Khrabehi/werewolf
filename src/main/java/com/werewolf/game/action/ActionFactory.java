package com.werewolf.game.action;

import java.util.Map;

public class ActionFactory {
    private static final Map<String, GameAction> ACTIONS = Map.ofEntries(
        Map.entry("KILL", new KillAction()),
        Map.entry("VOTE", new VoteAction()),
        Map.entry("PROTECT", new ProtectAction()),
        Map.entry("INVESTIGATE", new InvestigateAction())
    );
    
    public GameAction getAction(String actionType) {
        GameAction action = ACTIONS.get(actionType.toUpperCase());
        if (action == null) {
            throw new IllegalArgumentException("Unknown action: " + actionType);
        }
        return action;
    }
}