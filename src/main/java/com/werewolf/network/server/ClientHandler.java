package com.werewolf.network.server;

import com.werewolf.event.GameStateObserver;
import com.werewolf.event.GameStateUpdate;
import com.werewolf.game.GameSession;
import com.werewolf.network.shared.GameCommand;
import com.werewolf.network.shared.Message;
import com.werewolf.network.shared.MessageType;
import com.werewolf.validation.CommandExecutionResult;
import com.werewolf.validation.CommandOrchestrator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable, GameStateObserver {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private GameSession gameSession;
    private CommandOrchestrator orchestrator;
    private String playerId;

    public ClientHandler(Socket socket, String playerId, GameSession session) {
        this.socket = socket;
        this.playerId = playerId;
        this.gameSession = session;
        this.orchestrator = new CommandOrchestrator(session);

        session.subscribe(this);
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Nouveau client authentifié : " + playerId);

            while (true) {
                Message receivedMessage = (Message) in.readObject();
                handleMessage(receivedMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Déconnexion du client " + playerId);
        } finally {
            gameSession.unsubscribe(this);
            closeConnections();
        }
    }

    private void handleMessage(Message message) throws IOException {
        switch (message.getType()) {
            case PING:
                handlePing();
                break;
            case KILL:
            case VOTE:
            case HEAL:
            case PEEK:
                handleGameCommand(message);
                break;
            default:
                System.out.println("Type de message non géré : " + message.getType());
                break;
        }
    }

    private void handlePing() throws IOException {
        Message pongMessage = new Message(MessageType.PONG, "Server", "Return pong");
        out.writeObject(pongMessage);
        out.flush();
    }

    private void handleGameCommand(Message message) throws IOException {
        GameCommand cmd = (GameCommand) message.getContent();

        // Orchestrator gère validation + exécution
        CommandExecutionResult result = orchestrator.executeCommand(playerId, cmd);

        if (!result.isSuccess()) {
            Message errorResponse = new Message(
                    MessageType.ERROR,
                    "Server",
                    result.getErrorMessage());
            out.writeObject(errorResponse);
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

            synchronized (out) {
                if (out != null) {
                    out.writeObject(notification);
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Échec de l'envoi de la mise à jour au client : " + e.getMessage());
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