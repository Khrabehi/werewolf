package com.werewolf.game.role;

import java.io.Serializable;
import java.util.List;

public interface PlayerRole extends Serializable {
    String getName();

    /**
     * Determines whether this role allows the specified action type.
     *
     * @param actionType the action type to check for this role
     * @return {@code true} if this role can perform the specified action type; {@code false} otherwise
     */
    boolean canPerform(String actionType);

    List<String> getAllowedActions();
}
