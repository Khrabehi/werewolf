package application.event.impl;

import application.event.GameEvent;

/**
 * Événement d'attribution de rôle
 */
public class RoleAssignedEvent extends GameEvent {
    private final String playerId;
    private final String roleName;

    public RoleAssignedEvent(String playerId, String roleName) {
        super("ROLE_ASSIGNED");
        this.playerId = playerId;
        this.roleName = roleName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public String getMessage() {
        return "ROLE " + roleName;
    }
}
