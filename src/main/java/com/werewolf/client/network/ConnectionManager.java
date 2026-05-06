package com.werewolf.client.network;

import com.werewolf.client.model.ConnectionConfig;
import com.werewolf.client.model.MainMenuModel;
import com.werewolf.security.CertificateManager;
import com.werewolf.security.SSLContextFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ConnectionManager {
    private final SSLContext sslContext;
    private final MainMenuModel model;
    private final Consumer<Boolean> onConnectionResult;

    private static final String STORE_PASSWORD = loadStorePassword();

    private SSLSocket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ConnectionManager(MainMenuModel model, Consumer<Boolean> onConnectionResult) {
        this.model = model;
        this.onConnectionResult = onConnectionResult;
        this.sslContext = createSslContext();
    }

    private static SSLContext createSslContext() {
        try {
            return SSLContextFactory.createClientSSLContext(
                CertificateManager.CLIENT_KEYSTORE,
                STORE_PASSWORD,
                CertificateManager.CLIENT_TRUSTSTORE,
                STORE_PASSWORD
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
