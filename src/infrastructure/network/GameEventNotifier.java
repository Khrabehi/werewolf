package infrastructure.network;

import application.event.GameEvent;
import application.event.impl.RoleAssignedEvent;
import domain.model.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Notifies game events to clients - OCP Principle
 * Observer Pattern for event notification
 */
public class GameEventNotifier {
    private final List<ClientHandler> clients;

    public GameEventNotifier() {
        this.clients = new CopyOnWriteArrayList<>();
    }

    /**
     * Registers a client to receive notifications
     */
    public void registerClient(ClientHandler client) {
        clients.add(client);
    }

    /**
     * Unregisters a client
     */
    public void unregisterClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Notifies an event to all clients or a specific client
     */
    public void notifyEvent(GameEvent event, Player sourcePlayer) {
        // Personal events (e.g., role assignment)
        if (event instanceof RoleAssignedEvent) {
            RoleAssignedEvent roleEvent = (RoleAssignedEvent) event;
            notifyPlayer(roleEvent.getPlayerId(), event.getMessage());
        } else {
            // Global events
            broadcast(event.getMessage());
        }
    }

    /**
     * Sends a message to all clients
     */
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    /**
     * Sends a message to a specific player
     */
    public void notifyPlayer(String playerId, String message) {
        clients.stream()
                .filter(c -> c.getPlayer().getId().equals(playerId))
                .forEach(c -> c.send(message));
    }

    /**
     * Notifies a player's disconnection
     */
    public void notifyDisconnection(Player player) {
        unregisterClient(findClientByPlayer(player));
        if (player.getPseudo() != null) {
            broadcast("MESSAGE " + player.getPseudo() + " left the game.");
        }
    }

    private ClientHandler findClientByPlayer(Player player) {
        return clients.stream()
                .filter(c -> c.getPlayer().equals(player))
                .findFirst()
                .orElse(null);
    }

    /**
     * Sends the player list to all clients
     */
    public void broadcastPlayerList(List<String> playerNames) {
        StringBuilder playerList = new StringBuilder("PLAYER_LIST ");
        for (String name : playerNames) {
            playerList.append(name).append(" ");
        }
        broadcast(playerList.toString().trim());
    }

    /**
     * Notifies a player that they are now the admin
     */
    public void notifyAdminStatus(Player newAdmin) {
        for(ClientHandler client : clients){
            if(client.getPlayer().equals(newAdmin)){
                client.send("MESSAGE You are now the game administrator. Use START to begin the game.");
                break;
            }
        }
    }
}
