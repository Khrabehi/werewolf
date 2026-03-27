package com.werewolf.network.server;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.werewolf.security.CertificateManager;
import com.werewolf.security.SSLContextFactory;

public class GameServer {
    private static final int PORT = 8443; // Standard port for HTTPS
    private static final int MAX_PLAYERS = 10;

    private static final String STORE_PASSWORD = "werewolf_pass";

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

                // Forces the server to reject any connection from a client that does not
                // present a certificate signed by our CA.
                serverSocket.setNeedClientAuth(true);

                System.out.println("Werewolf Secure Server (mTLS) started on port " + PORT);
                System.out.println("Waiting for secure players (Max " + MAX_PLAYERS + ")...");

                while (true) {
                    // The accept() method now performs the TLS Handshake in the background
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Encrypted connection established with: " + clientSocket.getInetAddress());

                    // The ClientHandler handles the stream as before (encryption is transparent) to it
                    ClientHandler handler = new ClientHandler(clientSocket);
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
