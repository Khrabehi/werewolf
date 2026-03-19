package com.werewolf.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 8080;
    private static final int MAX_PLAYERS = 10;

    // Thread pool to not overused the server
    private ExecutorService threadPool;

    public GameServer() {
        this.threadPool = Executors.newFixedThreadPool(MAX_PLAYERS);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Werewolf server started on port " + PORT);
            System.out.println("Waiting for players (Max " + MAX_PLAYERS + ")...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection from : " + clientSocket.getInetAddress());
                
                // Give the client to handler
                ClientHandler handler = new ClientHandler(clientSocket);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Erreur de démarrage du serveur : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new GameServer().start();
    }
}
