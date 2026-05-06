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
     * Traite une GameStateUpdate reçue du serveur et met à jour le GameModel
     * en conséquence. Appelé depuis le thread d'écoute du ConnectionManager ; toutes les mises à jour
     * JavaFX résultantes sont réparties via Platform.runLater dans les écouteurs du modèle.
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

        // Assigner le rôle privé (envoyé une fois au début de la partie)
        if (metadata.containsKey("role")) {
            model.setMyRole((String) metadata.get("role"));
        }

        // Fin de la partie
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

        // Changement de phase → réinitialiser l'état de l'action
        if (phase != null && phase != model.getGamePhase()) {
            model.setGamePhase(phase);
            model.setHasActedThisPhase(false);
            model.setCanAct(false);
        }

        // Indication privée de nuit → ce joueur peut agir cette nuit
        if (Boolean.TRUE.equals(metadata.get("prompt"))) {
            model.setCanAct(true);
        }

        // Pendant le vote de jour, chaque joueur en vie a le droit de voter
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
     * Envoie la commande d'action de nuit appropriée en fonction du rôle du joueur.
     *
     * @param targetUsername pseudo du joueur cible
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
        model.addEventLog("Action envoyée. En attente des autres joueurs...");
    }

    /**
     * Envoie une commande de vote pour la phase de vote de jour.
     *
     * @param targetUsername pseudo du joueur contre lequel voter
     */
    public void performVote(String targetUsername) {
        if (!model.isCanAct() || model.isHasActedThisPhase()) return;

        String targetId = model.findPlayerIdByUsername(targetUsername);
        if (targetId == null) return;

        connectionManager.sendGameCommand(MessageType.VOTE, targetId, model.getMyUsername());
        model.setHasActedThisPhase(true);
        model.setCanAct(false);
        model.addEventLog("Vote envoyé contre " + targetUsername + ". En attente des autres...");
    }

    /**
     * Diffuse un message de chat aux autres joueurs.
     * @param message Contenu textuel du message.
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
