package infrastructure.network;

import application.event.GameEvent;
import application.event.impl.RoleAssignedEvent;
import domain.model.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notifie les événements du jeu aux clients - Principe OCP
 * Observer Pattern pour la notification d'événements
 */
public class GameEventNotifier {
    private final List<ClientHandler> clients;

    public GameEventNotifier() {
        this.clients = new CopyOnWriteArrayList<>();
    }

    /**
     * Enregistre un client pour recevoir les notifications
     */
    public void registerClient(ClientHandler client) {
        clients.add(client);
    }

    /**
     * Désenregistre un client
     */
    public void unregisterClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Notifie un événement à tous les clients ou un client spécifique
     */
    public void notifyEvent(GameEvent event, Player sourcePlayer) {
        // Événements personnels (ex: attribution de rôle)
        if (event instanceof RoleAssignedEvent) {
            RoleAssignedEvent roleEvent = (RoleAssignedEvent) event;
            notifyPlayer(roleEvent.getPlayerId(), event.getMessage());
        } else {
            // Événements globaux
            broadcast(event.getMessage());
        }
    }

    /**
     * Envoie un message à tous les clients
     */
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    /**
     * Envoie un message à un joueur spécifique
     */
    public void notifyPlayer(String playerId, String message) {
        clients.stream()
                .filter(c -> c.getPlayer().getId().equals(playerId))
                .forEach(c -> c.send(message));
    }

    /**
     * Notifie la déconnexion d'un joueur
     */
    public void notifyDisconnection(Player player) {
        unregisterClient(findClientByPlayer(player));
        if (player.getPseudo() != null) {
            broadcast("MESSAGE " + player.getPseudo() + " a quitté la partie.");
        }
    }

    private ClientHandler findClientByPlayer(Player player) {
        return clients.stream()
                .filter(c -> c.getPlayer().equals(player))
                .findFirst()
                .orElse(null);
    }

    /**
     * Envoie la liste des joueurs à tous les clients
     */
    public void broadcastPlayerList(List<String> playerNames) {
        StringBuilder playerList = new StringBuilder("PLAYER_LIST ");
        for (String name : playerNames) {
            playerList.append(name).append(" ");
        }
        broadcast(playerList.toString().trim());
    }
}
