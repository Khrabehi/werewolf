package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.Role;

public class GameServer {
    private static final int PORT = 12345; 
    // Liste des clients connectés 
    // Utilisation de Collections.synchronizedList pour la sécurité des threads
    private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

    private static final int MAX_PLAYERS = 10;
    private static final int MIN_PLAYERS = 4;
    private boolean gameStarted = false;

    
    public void start() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT); 
        System.out.println("Serveur démarré sur le port " + PORT);

        while (true) {
            Socket client = serverSocket.accept();

            // Refuse la connexion si le jeu a déjà commencé ou si le nombre maximum de joueurs est atteint
            if(clients.size() >= MAX_PLAYERS || gameStarted) {
                client.getOutputStream().write("MESSAGE Le jeu a déjà commencé ou le nombre maximum de joueurs est atteint. Connexion refusée.\n".getBytes());
                client.close();
                continue;
            }


            ClientHandler clientHandler = new ClientHandler(client, this);
            clients.add(clientHandler);
            new Thread(clientHandler).start();

            if(clients.size() >= MIN_PLAYERS && !gameStarted) {
                startGame();
            }
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public void sendPlayerList() {
        StringBuilder playerList = new StringBuilder("PLAYER_LIST ");
        for (ClientHandler client : clients) {
            if(client.getPseudo() != null){
                playerList.append(client.getPseudo()).append(" ");
            }
        }
        broadcast(playerList.toString().trim());
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void startGame() {
        if (!areAllPlayersReady()) {
            broadcast("MESSAGE Impossible de démarrer : tous les joueurs doivent avoir un pseudo (4-10 joueurs)");
            return;
        }
        gameStarted = true;
        broadcast("MESSAGE La partie commence !");
        assignRoles();
        broadcast("PHASE NUIT");
    }

    private void assignRoles() {
        List<Role> roles = new ArrayList<>();
        int numPlayers = clients.size();
        int numWolves = Math.max(1, numPlayers / 4); // Au moins 1 loup pour 4 joueurs
        for (int i = 0; i < numWolves; i++) {
            roles.add(Role.LOUP);
        }
        for (int i = numWolves; i < numPlayers; i++) {
            roles.add(Role.VILLAGEOIS);
        }
        Collections.shuffle(roles); // Mélange les rôles
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).setRole(roles.get(i));
        }
    }

    private boolean areAllPlayersReady() {
        if (clients.size() < MIN_PLAYERS || clients.size() > MAX_PLAYERS) {
            return false;
        }
        for (ClientHandler client : clients) {
            if (client.getPseudo() == null || client.getPseudo().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }


    public static void main(String[] args) throws Exception {
        GameServer server = new GameServer();
        server.start();
    }
}
