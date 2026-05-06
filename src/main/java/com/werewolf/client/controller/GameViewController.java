package com.werewolf.client.controller;

import com.werewolf.client.model.GameModel;
import com.werewolf.client.network.ConnectionManager;
import com.werewolf.event.GameStateUpdate;
import com.werewolf.game.GameState;
import com.werewolf.game.Player;
import com.werewolf.network.shared.MessageType;

import java.util.List;
import java.util.Map;

public class GameViewController {

    private final GameModel model;
    private final ConnectionManager connectionManager;

    public GameViewController(GameModel model, ConnectionManager connectionManager) {
        this.model = model;
        this.connectionManager = connectionManager;
    }

    /**
     * Processes a GameStateUpdate received from the server and updates the GameModel
     * accordingly. Called from the ConnectionManager listener thread; all resulting
     * JavaFX updates are dispatched via Platform.runLater inside the model listeners.
     */
    public void processGameStateUpdate(GameStateUpdate update) {
        GameState phase = update.getNewPhase();
        String message = update.getMessage();
        List<Player> alivePlayers = update.getAlivePlayers();
        Map<String, Object> metadata = update.getMetadata();

        if (alivePlayers != null && !alivePlayers.isEmpty()) {
            model.setAlivePlayers(alivePlayers);
        }

        if (message != null && !message.isBlank()) {
            model.addEventLog(message);
        }

        // Private role assignment (sent once at game start)
        if (metadata.containsKey("role")) {
            model.setMyRole((String) metadata.get("role"));
        }

        // Game over
        if (phase == GameState.GAME_OVER) {
            model.setGamePhase(GameState.GAME_OVER);
            model.setCanAct(false);
            if (metadata.containsKey("winner")) {
                model.setGameOverWinner((String) metadata.get("winner"));
            }
            if (metadata.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                Map<String, String> roles = (Map<String, String>) metadata.get("roles");
                model.setAllRoles(roles);
            }
            return;
        }

        // Phase change → reset action state
        if (phase != null && phase != model.getGamePhase()) {
            model.setGamePhase(phase);
            model.setHasActedThisPhase(false);
            model.setCanAct(false);
        }

        // Private night prompt → this player can act tonight
        if (Boolean.TRUE.equals(metadata.get("prompt"))) {
            model.setCanAct(true);
        }

        // During day voting, every alive player gets to vote
        if (phase == GameState.DAY_VOTING) {
            boolean isAlive = model.getAlivePlayers().stream()
                    .anyMatch(p -> p.getUsername().equals(model.getMyUsername()));
            if (isAlive) {
                model.setCanAct(true);
            } else {
                model.setCanAct(false);
            }
        }
    }

    /**
     * Sends the appropriate night action command based on the player's role.
     *
     * @param targetUsername display name of the target player
     */
    public void performNightAction(String targetUsername) {
        if (!model.isCanAct() || model.isHasActedThisPhase()) return;

        String targetId = model.findPlayerIdByUsername(targetUsername);
        if (targetId == null) return;

        String role = model.getMyRole();
        if (role == null) return;

        MessageType type = switch (role) {
            case "Werewolf" -> MessageType.KILL;
            case "Seer"     -> MessageType.PEEK;
            case "Medic"    -> MessageType.HEAL;
            default -> null;
        };

        if (type == null) return;

        connectionManager.sendGameCommand(type, targetId, model.getMyUsername());
        model.setHasActedThisPhase(true);
        model.setCanAct(false);
        model.addEventLog("Action sent. Waiting for other players...");
    }

    /**
     * Sends a vote command for the day-voting phase.
     *
     * @param targetUsername display name of the player to vote against
     */
    public void performVote(String targetUsername) {
        if (!model.isCanAct() || model.isHasActedThisPhase()) return;

        String targetId = model.findPlayerIdByUsername(targetUsername);
        if (targetId == null) return;

        connectionManager.sendGameCommand(MessageType.VOTE, targetId, model.getMyUsername());
        model.setHasActedThisPhase(true);
        model.setCanAct(false);
        model.addEventLog("Vote sent for " + targetUsername + ". Waiting for others...");
    }

    /**
     * Dispatches a chat message to other players.
     * @param message Text contents of the chat message.
     */
    public void sendChat(String message) {
        if (message == null || message.isBlank()) return;
        
        if (model.getGamePhase() == GameState.NIGHT) {
            return;
        }

        boolean isAlive = model.getAlivePlayers().stream()
                .anyMatch(p -> p.getUsername().equals(model.getMyUsername()));
        if (!isAlive) {
            return;
        }
        
        connectionManager.sendGameCommand(MessageType.CHAT, message, model.getMyUsername());
    }
}
