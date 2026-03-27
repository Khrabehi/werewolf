package com.werewolf.network.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.werewolf.network.shared.Message;
import com.werewolf.network.shared.MessageType;
import com.werewolf.security.CertificateManager;
import com.werewolf.security.SSLContextFactory;

public class GameClient {

    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;

    private static final String STORE_PASSWORD = "werewolf_pass";

    public GameClient(String serverAddress, int serverPort, String playerName) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.playerName = playerName;
    }

    public void connect() {
        System.out.println("Checking client certificate infrastructure...");
        // Ensure certificates exist before trying to connect
        CertificateManager.initializeCertificates(STORE_PASSWORD);
        try {
            // Create SSL Context with Client Identity and Server Validation
            SSLContext sslContext = SSLContextFactory.createClientSSLContext(
                    CertificateManager.CLIENT_KEYSTORE, STORE_PASSWORD,
                    CertificateManager.CLIENT_TRUSTSTORE, STORE_PASSWORD);

            SSLSocketFactory ssf = sslContext.getSocketFactory();
            socket = (SSLSocket) ssf.createSocket(serverAddress, serverPort);
            ((SSLSocket) socket).startHandshake(); // Explicitly start the TLS handshake
            System.out.println("Securely connected (mTLS) to server " + serverAddress + ":" + serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Thread listenerThread = new Thread(this::listenForMessages);
            listenerThread.start();

            Message pingMessage = new Message(MessageType.PING, playerName, "Hello secure world!");
            sendMessage(pingMessage);

        } catch (Exception e) {
            System.err.println("Error occurred while connecting to the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(Message message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
                System.out.println("-> Send : " + message.getType());
            }
        } catch (IOException e) {
            System.err.println("Error during the sending of a message : " + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            while (true) {
                Message receivedMessage = (Message) in.readObject();
                System.out.println("<- Received from server : " + receivedMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection with the server lost");
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
            System.out.println("Disconnected properly.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GameClient client = new GameClient("127.0.0.1", 8443, "Joueur1");
        client.connect();
    }
}
