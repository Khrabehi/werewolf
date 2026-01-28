package infrastructure.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Manages a client's network connection - SRP Principle
 * Responsible only for network communication
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
     * Sends a message to the client
     */
    public void send(String message) {
        if (connected) {
            out.println(message);
        }
    }

    /**
     * Reads a line from the client
     */
    public String readLine() throws IOException {
        return in.readLine();
    }

    /**
     * Closes the connection
     */
    public void close() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public String getConnectionId() {
        return connectionId;
    }

    public boolean isConnected() {
        return connected && !socket.isClosed();
    }
}
