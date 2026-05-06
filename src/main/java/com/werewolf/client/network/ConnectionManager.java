package com.werewolf.client.network;

import com.werewolf.client.model.ConnectionConfig;
import com.werewolf.client.model.MainMenuModel;
import com.werewolf.event.GameStateUpdate;
import com.werewolf.network.shared.JoinGameRequest;
import com.werewolf.network.shared.Message;
import com.werewolf.network.shared.MessageType;
import com.werewolf.network.shared.PlayerListUpdate;
import com.werewolf.security.CertificateManager;
import com.werewolf.security.SSLContextFactory;

import com.werewolf.network.shared.GameCommand;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ConnectionManager {
    private final MainMenuModel model;
    private final Consumer<Boolean> onConnectionResult;

    private SSLSocket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**
     * Routes GAME_STATE_UPDATE messages to the game view once the game starts.
     * Updates received before this handler is set are buffered and replayed on registration.
     */
    private volatile Consumer<GameStateUpdate> gameStateUpdateHandler = null;
    private final List<GameStateUpdate> pendingGameUpdates = new ArrayList<>();

    public ConnectionManager(MainMenuModel model, Consumer<Boolean> onConnectionResult) {
        this.model = model;
        this.onConnectionResult = onConnectionResult;
    }

    private static SSLContext createSslContext() {
        try {
            String storePassword = loadStorePassword();
            return SSLContextFactory.createClientSSLContext(
                CertificateManager.CLIENT_KEYSTORE,
                storePassword,
                CertificateManager.CLIENT_TRUSTSTORE,
                storePassword
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize SSL context", e);
        }
    }

    private static String loadStorePassword() {
        String password = System.getProperty("GAMECLIENT_STORE_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = System.getenv("GAMECLIENT_STORE_PASSWORD");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException(
                "Keystore password not configured. Set system property or environment variable 'GAMECLIENT_STORE_PASSWORD'."
            );
        }
        return password;
    }

    public void connectAsync(ConnectionConfig config) {
        Thread connectionThread = new Thread(() -> {
            try {
                connect(config);
            } catch (Exception e) {
                handleConnectionError(e);
            }
        });
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void connect(ConnectionConfig config) throws IOException {
        model.setStatusMessage("Connecting to " + config.getIpAddress() + ":" + config.getPort() + "...");

        SSLContext sslContext = createSslContext();
        SSLSocketFactory factory = sslContext.getSocketFactory();
        socket = (SSLSocket) factory.createSocket(
            config.getIpAddress(),
            config.getPort()
        );

        socket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
        socket.startHandshake();

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        model.setStatusMessage("Successfully connected to server!");
        model.setIsConnecting(false);

        startListener();
        sendJoinGame(config.getUsername());

        onConnectionResult.accept(true);
    }

    private void handleConnectionError(Exception e) {
        model.setIsConnecting(false);
        String errorMessage = "Connection failed: " + e.getMessage();
        model.setStatusMessage(errorMessage);
        onConnectionResult.accept(false);

        System.err.println(errorMessage);
        e.printStackTrace();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    public void sendJoinGame(String username) {
        if (!isConnected() || out == null) {
            return;
        }
        try {
            JoinGameRequest request = new JoinGameRequest(username);
            Message message = new Message(MessageType.JOIN_GAME, username, request);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    public void sendStartGame(String username) {
        if (!isConnected() || out == null) {
            return;
        }
        try {
            Message message = new Message(MessageType.START_GAME, username, "start");
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    private void startListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                while (socket != null && socket.isConnected()) {
                    Object incoming = in.readObject();
                    if (incoming instanceof Message) {
                        handleIncomingMessage((Message) incoming);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                handleConnectionError(e);
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleIncomingMessage(Message message) {
        switch (message.getType()) {
            case PLAYER_LIST_UPDATE:
                Object content = message.getContent();
                if (content instanceof PlayerListUpdate) {
                    PlayerListUpdate update = (PlayerListUpdate) content;
                    model.setPlayerNames(update.getPlayerNames());
                    model.setAdminName(update.getAdminName());
                }
                break;
            case GAME_STARTED:
                model.setStatusMessage("Game started.");
                if (!model.isGameStarted()) {
                    model.setGameStarted(true);
                }
                break;
            case GAME_STATE_UPDATE:
                Object updateContent = message.getContent();
                if (updateContent instanceof GameStateUpdate gameUpdate) {
                    routeGameStateUpdate(gameUpdate);
                } else if (updateContent != null) {
                    model.setStatusMessage(updateContent.toString());
                }
                break;
            case ERROR:
                if (message.getContent() != null) {
                    model.setStatusMessage("Server error: " + message.getContent());
                } else {
                    model.setStatusMessage("Server error.");
                }
                break;
            default:
                break;
        }
    }

    private void routeGameStateUpdate(GameStateUpdate update) {
        Consumer<GameStateUpdate> handler = gameStateUpdateHandler;
        if (handler != null) {
            handler.accept(update);
        } else {
            // Buffer updates that arrive before the game view is ready (e.g. role assignment)
            synchronized (pendingGameUpdates) {
                pendingGameUpdates.add(update);
            }
            // Also show the message in the lobby status bar
            if (update.getMessage() != null) {
                model.setStatusMessage(update.getMessage());
            }
        }
    }

    /**
     * Registers the game-phase message handler. Immediately replays any updates that
     * arrived before the game view was initialised (e.g. private role assignment).
     */
    public void setGameStateUpdateHandler(Consumer<GameStateUpdate> handler) {
        this.gameStateUpdateHandler = handler;
        List<GameStateUpdate> buffered;
        synchronized (pendingGameUpdates) {
            buffered = new ArrayList<>(pendingGameUpdates);
            pendingGameUpdates.clear();
        }
        for (GameStateUpdate update : buffered) {
            handler.accept(update);
        }
    }

    public void clearGameStateUpdateHandler() {
        this.gameStateUpdateHandler = null;
        synchronized (pendingGameUpdates) {
            pendingGameUpdates.clear();
        }
    }

    /**
     * Sends a game action command (KILL / VOTE / HEAL / PEEK) to the server.
     *
     * @param type           the message type that identifies the action
     * @param targetId       the server-assigned player ID of the target
     * @param senderUsername this client's display name (used as message sender)
     */
    public void sendGameCommand(MessageType type, String targetId, String senderUsername) {
        if (!isConnected() || out == null) return;
        try {
            GameCommand cmd = new GameCommand(type.name(), targetId);
            Message message = new Message(type, senderUsername, cmd);
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    public ObjectOutputStream getOutputStream() {
        return out;
    }

    public ObjectInputStream getInputStream() {
        return in;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }
}
