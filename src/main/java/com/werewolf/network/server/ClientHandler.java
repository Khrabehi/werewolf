package com.werewolf.network.server;

import com.werewolf.event.GameStateObserver;
import com.werewolf.event.GameStateUpdate;
import com.werewolf.game.GameManager;
import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;
import com.werewolf.network.shared.JoinGameRequest;
import com.werewolf.network.shared.Message;
import com.werewolf.network.shared.MessageType;
import com.werewolf.network.shared.PlayerListUpdate;
import com.werewolf.validation.CommandExecutionResult;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable, GameStateObserver {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private final Object outLock = new Object();

    private GameSession gameSession;
    private String playerId;
    private final GameManager gameManager;

    public ClientHandler(Socket socket, String playerId, GameSession session, GameManager gameManager) {
        this.socket = socket;
        this.playerId = playerId;
        this.gameSession = session;
        this.gameManager = gameManager;

        session.subscribe(this);
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Nouveau client authentifié : " + playerId);

            // enregistre la connexion à broadcast
            PlayerConnectionManager.registerConnection(playerId, this);

            while (true) {
                Message receivedMessage = (Message) in.readObject();
                handleMessage(receivedMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Déconnexion du client " + playerId);
        } finally {
            // Notifie qu'une connexion a été perdu 
            PlayerConnectionManager.unregisterConnection(playerId);

            if (gameSession != null && playerId != null) {
                gameSession.removePlayer(playerId);

                // Diffuse la liste de joueurs mise à jour
                try {
                    String adminName = getAdminName();
                    java.util.List<String> playerNames = gameSession.getPlayerNames();
                    PlayerListUpdate update = new PlayerListUpdate(playerNames, adminName);
                    Message notification = new Message(
                            MessageType.PLAYER_LIST_UPDATE,
                            "Server",
                            update);
                    PlayerConnectionManager.broadcastToAll(notification);
                } catch (Exception e) {
                    System.err.println("Failed to broadcast disconnect update: " + e.getMessage());
                }
            }
            if (gameSession != null) {
                gameSession.unsubscribe(this);
            }
            closeConnections();
        }
    }

    private void handleMessage(Message message) throws IOException {
        switch (message.getType()) {
            case PING:
                handlePing();
                break;
            case JOIN_GAME:
                handleJoinGame(message);
                break;
            case START_GAME:
                handleStartGame();
                break;
            case KILL:
            case VOTE:
            case HEAL:
            case PEEK:
                handleGameCommand(message);
                break;
            case CHAT:
                handleChat(message);
                break;
            default:
                System.out.println("Type de message non géré : " + message.getType());
                break;
        }
    }

    private void handlePing() throws IOException {
        Message pongMessage = new Message(MessageType.PONG, "Server", "Return pong");
        synchronized (outLock) {
            out.writeObject(pongMessage);
            out.flush();
        }
    }

    private void handleJoinGame(Message message) throws IOException {
        Object content = message.getContent();
        String username = extractUsername(content);

        System.out.println("Player " + playerId + " joining with username: " + username);

        Player newPlayer = new Player(playerId, username);
        gameSession.addPlayer(newPlayer);

        // Assigne le role d'admin au premier joueur
        gameSession.assignAdminIfNeeded();

        java.util.List<String> playerNames = gameSession.getPlayerNames();
        String adminName = getAdminName();

        PlayerListUpdate update = new PlayerListUpdate(playerNames, adminName);
        Message notification = new Message(
                MessageType.PLAYER_LIST_UPDATE,
                "Server",
                update);
        PlayerConnectionManager.broadcastToAll(notification);
    }

    private String extractUsername(Object content) {
        if (content instanceof JoinGameRequest) {
            return ((JoinGameRequest) content).getUsername();
        }
        return (content != null) ? content.toString() : playerId;
    }

    private String getAdminName() {
        String adminId = gameSession.getAdminId();
        if (adminId == null) {
            return null;
        }
        Player admin = gameSession.getPlayer(adminId);
        return admin != null ? admin.getUsername() : null;
    }

    private void handleGameCommand(Message message) throws IOException {
        Object content = message.getContent();
        if (!(content instanceof GameCommand)) {
            Message errorResponse = new Message(
                    MessageType.ERROR,
                    "Server",
                    "Invalid command content.");
            synchronized (outLock) {
                out.writeObject(errorResponse);
                out.flush();
            }
            return;
        }

        GameCommand cmd = (GameCommand) content;

        CommandExecutionResult result = gameManager.handleCommand(playerId, cmd);

        if (!result.isSuccess()) {
            Message errorResponse = new Message(
                    MessageType.ERROR,
                    "Server",
                    result.getErrorMessage());
            synchronized (outLock) {
                out.writeObject(errorResponse);
                out.flush();
            }
            return;
        }

        Message ack = new Message(MessageType.GAME_COMMAND_RESPONSE, "Server", "Command accepted");
        synchronized (outLock) {
            out.writeObject(ack);
            out.flush();
        }
    }

    private void handleChat(Message message) throws IOException {
        if (gameSession == null) return;

        Object content = message.getContent();
        if (!(content instanceof GameCommand)) return;

        GameCommand cmd = (GameCommand) content;
        // Le texte du message est stocké dans le champ targetPlayerId par le client
        String chatText = cmd.getTargetPlayerId();

        Player sender = gameSession.getPlayer(playerId);
        if (sender == null || !sender.isAlive()) return;

        if (gameSession.getCurrentPhase() == com.werewolf.game.GameState.NIGHT) {
            return; // Pas de discussion la nuit pour les joueurs normaux
        }

        gameSession.notifySessionUpdate("[" + sender.getUsername() + "] : " + chatText);
    }

    private void handleStartGame() throws IOException {
        CommandExecutionResult result = gameManager.startGame(playerId);
        if (!result.isSuccess()) {
            Message errorResponse = new Message(
                    MessageType.ERROR,
                    "Server",
                    result.getErrorMessage());
            synchronized (outLock) {
                out.writeObject(errorResponse);
                out.flush();
            }
            return;
        }

        Message ack = new Message(MessageType.GAME_STARTED, "Server", "Game started");
        synchronized (outLock) {
            out.writeObject(ack);
            out.flush();
        }
    }

    @Override
    public void onGameStateUpdate(GameStateUpdate update) {
        try {
            Message notification = new Message(
                    MessageType.GAME_STATE_UPDATE,
                    "Server",
                    update);

            synchronized (outLock) {
                if (out != null) {
                    out.writeObject(notification);
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Échec de l'envoi de la mise à jour au client : " + e.getMessage());
        }
    }

    /**
     * Methode pour broadcast un message
     */
    public void sendMessage(Message message) throws IOException {
        synchronized (outLock) {
            if (out != null) {
                out.writeObject(message);
                out.flush();
            }
        }
    }

    private void closeConnections() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}