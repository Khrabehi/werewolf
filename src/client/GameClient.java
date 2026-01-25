package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket(HOST, PORT);
        System.out.println("Connecté au serveur " + HOST + " sur le port " + PORT);
        
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        //Thread qui écoute les messages du serveur
        new Thread(() -> {
            try {
                String msg; 
                while ((msg = in.readLine()) != null) {
                    System.out.println("[SERVEUR]: " + msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        //Lecture du clavier 
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String userInput = scanner.nextLine();
            out.println(userInput);
        }
    }
}
