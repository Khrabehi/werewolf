package infrastructure.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Gère la connexion réseau d'un client - Principe SRP
 * Responsable uniquement de la communication réseau
 */
public class ClientConnection {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final String connectionId;
    private boolean connected;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.connectionId = socket.getRemoteSocketAddress().toString();
        this.connected = true;
    }

    /**
     * Envoie un message au client
     */
    public void send(String message) {
        if (connected) {
            out.println(message);
        }
    }

    /**
     * Lit une ligne depuis le client
     */
    public String readLine() throws IOException {
        return in.readLine();
    }

    /**
     * Ferme la connexion
     */
    public void close() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }

    public String getConnectionId() {
        return connectionId;
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }
}
