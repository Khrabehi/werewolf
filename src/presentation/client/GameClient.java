package presentation.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client du jeu refactoré - Principe SRP
 * Responsable uniquement de l'interface utilisateur console
 */
public class GameClient {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connecté au serveur " + HOST + " sur le port " + PORT);
            
            // Thread pour écouter les messages du serveur
            Thread listenerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("[SERVEUR] " + message);
                    }
                } catch (Exception e) {
                    System.out.println("Connexion fermée.");
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            // Lecture des commandes utilisateur
            while (scanner.hasNextLine()) {
                String userInput = scanner.nextLine();
                out.println(userInput);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }
}
