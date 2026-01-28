package infrastructure.network;

import application.event.GameEvent;
import application.service.CommandService;
import application.service.GameService;
import domain.model.Player;

import java.io.IOException;
import java.util.List;

/**
 * Manages a connected client - SRP Principle
 * Bridges the network layer and the application layer
 */
public class ClientHandler implements Runnable {
    private final ClientConnection connection;
    private final CommandService commandService;
    private final Player player;
    private final GameEventNotifier eventNotifier;
    private final GameService gameService;

    public ClientHandler(ClientConnection connection,
            CommandService commandService,
            Player player,
            GameEventNotifier eventNotifier,
            GameService gameService) {
        this.connection = connection;
        this.commandService = commandService;
        this.player = player;
        this.eventNotifier = eventNotifier;
        this.gameService = gameService;
    }

    @Override
    public void run() {
        try {
            connection.send("MESSAGE Welcome! Enter your pseudo with: PSEUDO <your_pseudo>");

            String line;
            while ((line = connection.readLine()) != null) {
                // Process the command via the service
                List<GameEvent> events = commandService.handleCommand(line, player);

                // Notify events
                for (GameEvent event : events) {
                    eventNotifier.notifyEvent(event, player);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + player.getPseudo());
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        eventNotifier.unregisterClient(this);

        try {
            gameService.removePlayer(player);
            eventNotifier.broadcast("MESSAGE " + player.getPseudo() + " has left the game.");
            Player newAdmin = gameService.getGame().getAdmin();
            if(newAdmin != null) {
                eventNotifier.notifyAdminStatus(newAdmin);
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    public void send(String message) {
        connection.send(message);
    }

    public Player getPlayer() {
        return player;
    }
}
