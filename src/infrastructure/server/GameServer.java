package infrastructure.server;

import application.service.CommandService;
import application.service.GameService;
import application.service.VoteService;
import domain.model.GameConfiguration;
import domain.model.Player;
import infrastructure.network.ClientConnection;
import infrastructure.network.ClientHandler;
import infrastructure.network.GameEventNotifier;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Serveur de jeu refactoré - Principe SRP
 * Responsable uniquement de l'acceptation des connexions
 */
public class GameServer {
    private static final int PORT = 12345;
    
    private final GameService gameService;
    private final VoteService voteService;
    private final CommandService commandService;
    private final GameEventNotifier eventNotifier;

    public GameServer() {
        GameConfiguration config = GameConfiguration.defaultConfiguration();
        this.gameService = new GameService(config);
        this.voteService = new VoteService(gameService.getGame());
        this.commandService = new CommandService(gameService, voteService);
        this.eventNotifier = new GameEventNotifier();
    }

    /**
     * Démarre le serveur
     */
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur démarré sur le port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleNewClient(clientSocket);
            }
        }
    }

    /**
     * Gère un nouveau client
     */
    private void handleNewClient(Socket clientSocket) {
        try {
            // Vérifier si le jeu est plein ou déjà démarré
            if (gameService.getGame().isStarted() || 
                gameService.getGame().getPlayers().size() >= gameService.getGame().getMaxPlayers()) {
                rejectClient(clientSocket);
                return;
            }

            // Créer le joueur
            Player player = gameService.addPlayer();
            
            // Créer la connexion et le handler
            ClientConnection connection = new ClientConnection(clientSocket);
            ClientHandler handler = new ClientHandler(connection, commandService, player, eventNotifier);
            
            // Enregistrer le handler pour les notifications
            eventNotifier.registerClient(handler);
            
            // Mettre à jour la liste des joueurs
            eventNotifier.broadcastPlayerList(gameService.getPlayerList());
            
            // Démarrer le thread du handler
            new Thread(handler).start();
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la connexion du client: " + e.getMessage());
        }
    }

    /**
     * Refuse une connexion
     */
    private void rejectClient(Socket clientSocket) {
        try {
            ClientConnection connection = new ClientConnection(clientSocket);
            connection.send("MESSAGE Le jeu a déjà commencé ou est plein. Connexion refusée.");
            connection.close();
        } catch (IOException e) {
            System.err.println("Erreur lors du rejet du client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Erreur du serveur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
