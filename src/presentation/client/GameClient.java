package presentation.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Refactored game client - SRP Principle
 * Responsible only for the console user interface
 */
public class GameClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connected to server " + HOST + " on port " + PORT);
            
            // Thread to listen for server messages
            Thread listenerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("[SERVER] " + message);
                    }
                } catch (Exception e) {
                    System.out.println("Connection closed.");
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            // Read user commands
            while (scanner.hasNextLine()) {
                String userInput = scanner.nextLine();
                out.println(userInput);
            }
            
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}
