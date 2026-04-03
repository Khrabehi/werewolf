package com.werewolf.network.server;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.security.CertificateManager;
import com.werewolf.security.SSLContextFactory;

public class GameServer {
    private static final int PORT = 8443; // Standard port for HTTPS
    private static final int MAX_PLAYERS = 10;

    private static final String STORE_PASSWORD = loadStorePassword();

    private static String loadStorePassword() {
        String password = System.getProperty("GAMESERVER_STORE_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = System.getenv("GAMESERVER_STORE_PASSWORD");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException(
                    "Keystore password not configured. Set system property or environment variable 'GAMESERVER_STORE_PASSWORD'.");
        }
        return password;
    }

    // Thread pool to not overused the server
    private ExecutorService threadPool;

    public GameServer() {
        this.threadPool = Executors.newFixedThreadPool(MAX_PLAYERS);
    }

    public void start() {
        System.out.println("Checking and initializing certificate infrastructure...");
        CertificateManager.initializeCertificates(STORE_PASSWORD);
        try {
            // Create SSLContext for the server
            SSLContext sslContext = SSLContextFactory.createServerSSLContext(
                    CertificateManager.SERVER_KEYSTORE, STORE_PASSWORD,
                    CertificateManager.SERVER_TRUSTSTORE, STORE_PASSWORD);
            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            try (SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT)) {

                serverSocket.setNeedClientAuth(true);

                System.out.println("Werewolf Secure Server (mTLS) started on port " + PORT);
                System.out.println("Waiting for secure players (Max " + MAX_PLAYERS + ")...");

                final GameSession gameSession = new GameSession("main-session");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Connection from : " + clientSocket.getInetAddress());

                    String tempPlayerId = "Player-" + clientSocket.getPort();

                    Player newPlayer = new Player(tempPlayerId, tempPlayerId);
                    gameSession.addPlayer(newPlayer);

                    ClientHandler handler = new ClientHandler(clientSocket, tempPlayerId, gameSession);

                    threadPool.execute(handler);
                }
            }
        } catch (Exception e) {
            System.err.println("Critical error when starting the secure server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new GameServer().start();
    }
}
