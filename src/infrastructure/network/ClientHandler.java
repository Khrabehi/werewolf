package infrastructure.network;

import application.event.GameEvent;
import application.service.CommandService;
import domain.model.Player;

import java.io.IOException;

/**
 * Gère un client connecté - Principe SRP
 * Fait le pont entre la couche réseau et la couche application
 */
public class ClientHandler implements Runnable {
    private final ClientConnection connection;
    private final CommandService commandService;
    private final Player player;
    private final GameEventNotifier eventNotifier;

    public ClientHandler(ClientConnection connection, 
                        CommandService commandService,
                        Player player,
                        GameEventNotifier eventNotifier) {
        this.connection = connection;
        this.commandService = commandService;
        this.player = player;
        this.eventNotifier = eventNotifier;
    }

    @Override
    public void run() {
        try {
            connection.send("MESSAGE Bienvenue ! Entrez votre pseudo avec : PSEUDO <votre_pseudo>");
            
            String line;
            while ((line = connection.readLine()) != null) {
                // Traiter la commande via le service
                var events = commandService.handleCommand(line, player);
                
                // Notifier les événements
                for (GameEvent event : events) {
                    eventNotifier.notifyEvent(event, player);
                }
            }
        } catch (IOException e) {
            System.out.println("Client déconnecté: " + player.getPseudo());
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        connection.close();
        // Notifier la déconnexion via un événement
        eventNotifier.notifyDisconnection(player);
    }

    public void send(String message) {
        connection.send(message);
    }

    public Player getPlayer() {
        return player;
    }
}
