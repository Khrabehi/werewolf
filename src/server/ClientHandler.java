package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import common.Role;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameServer server;
    private String pseudo;
    private Role role;
    private boolean isAlive = true;

    public ClientHandler(Socket socket, GameServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setRole(Role role) {
        this.role = role;
        send("ROLE " + role);
    }

    public Role getRole() {
        return role;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void kill() {
        isAlive = false;
        send("DEAD");
    }

    @Override
    public void run() {
        try{
            out.println("[MESSAGE] Entrez votre pseudo avec : PSEUDO <votre_pseudo>");
            String line; 
            while ((line = in.readLine()) != null) {
                if (line.startsWith("PSEUDO ")) {
                    pseudo = line.substring(7);
                    server.broadcast("MESSAGE " + pseudo + " a rejoint la partie");
                    server.sendPlayerList();
                } else {
                    System.out.println("[" + pseudo + "] " + line);
                }
            }
        }catch(IOException e){
            System.out.println("Client déconnecté : " + pseudo);
            server.removeClient(this);
            server.broadcast("MESSAGE " + pseudo + " a quitté la partie");
        }finally{
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
    }

    public void send(String message) {
        out.println(message);
    }
}