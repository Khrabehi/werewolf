package com.werewolf.game.role;

import java.util.List;

public interface PlayerRole {
    String getName();

    /**
     * Determines if the player can perform their role's action during the current phase of the game.
     * @return
     */
    boolean canPerform(String actionType);

    List<String> getAllowedActions();
}
