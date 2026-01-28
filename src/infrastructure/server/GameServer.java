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
 * Refactored game server - SRP Principle
 * Responsible only for accepting connections
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
     * Starts the server
     */
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleNewClient(clientSocket);
            }
        }
    }

    /**
     * Handles a new client
     */
    private void handleNewClient(Socket clientSocket) {
        try {
            // Check if the game is full or already started
            if (gameService.getGame().isStarted() || 
                gameService.getGame().getPlayers().size() >= gameService.getGame().getMaxPlayers()) {
                rejectClient(clientSocket);
                return;
            }

            // Create the player
            Player player = gameService.addPlayer();
            
            // Create the connection and handler
            ClientConnection connection = new ClientConnection(clientSocket);
            ClientHandler handler = new ClientHandler(connection, commandService, player, eventNotifier, gameService);
            boolean isAdmin = gameService.getGame().getAdmin().equals(player);

            // Register the handler for notifications
            eventNotifier.registerClient(handler);
            
            connection.send("MESSAGE Welcome to the game !");
            
            if(isAdmin){
                connection.send("MESSAGE You are the administrator of the game. Use start to begin the game.");
            }
            
            // Start the handler thread
            new Thread(handler).start();
            
        } catch (IOException e) {
            System.err.println("Error connecting client: " + e.getMessage());
        }
    }

    /**
     * Rejects a connection
     */
    private void rejectClient(Socket clientSocket) {
        try {
            ClientConnection connection = new ClientConnection(clientSocket);
            connection.send("MESSAGE The game has already started or is full. Connection refused.");
            connection.close();
        } catch (IOException e) {
            System.err.println("Error rejecting client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
