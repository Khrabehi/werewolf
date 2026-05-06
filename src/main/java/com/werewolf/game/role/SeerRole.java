package com.werewolf.game.role;

import java.util.List;

public class SeerRole implements PlayerRole {
    private static final List<String> ALLOWED_ACTIONS = List.of("PEEK", "VOTE");

    @Override
    public String getName() { return "Seer"; }

    @Override
    public boolean canPerform(String actionType) {
        return ALLOWED_ACTIONS.contains(actionType);
    }

    @Override
    public List<String> getAllowedActions() {
        return ALLOWED_ACTIONS;
    }
    
}
