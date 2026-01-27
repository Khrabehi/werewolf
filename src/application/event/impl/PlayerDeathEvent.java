package application.event.impl;

import application.event.GameEvent;

/**
 * Événement de mort d'un joueur
 */
public class PlayerDeathEvent extends GameEvent {
    private final String playerPseudo;
    private final String reason;

    public PlayerDeathEvent(String playerPseudo, String reason) {
        super("PLAYER_DEATH");
        this.playerPseudo = playerPseudo;
        this.reason = reason;
    }

    public String getPlayerPseudo() {
        return playerPseudo;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getMessage() {
        return "MESSAGE " + playerPseudo + " est mort ! (" + reason + ")";
    }
}
