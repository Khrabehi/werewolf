package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import common.Phase;
import common.Role;

public class GameServer {
    private static final int PORT = 12345; 
    // Liste des clients connectés 
    // Utilisation de Collections.synchronizedList pour la sécurité des threads
    private List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private ClientHandler adminClient = null;

    private static final int MAX_PLAYERS = 10;
    private static final int MIN_PLAYERS = 4;
    private boolean gameStarted = false;
    private Phase currentPhase = Phase.NUIT;
    private Map<String, Integer> killVotes = new HashMap<>();

    
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

            if(adminClient == null) {
                adminClient = clientHandler;
                clientHandler.send("MESSAGE Vous êtes l'administrateur du jeu.");
            }

            new Thread(clientHandler).start();
        }
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        if (adminClient == clientHandler) {
            reassignAdmin();
        }
        sendPlayerList();
    }

    private void reassignAdmin() {
        synchronized (clients) {
            if (clients.isEmpty()) {
                adminClient = null;
                return;
            }
            adminClient = clients.get(0);
            adminClient.send("MESSAGE Vous devenez l'administrateur du jeu. Envoyez START pour lancer la partie.");
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.send(message);
        }
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
        currentPhase = Phase.NUIT;
        broadcast("PHASE NUIT");
        startNight();
    }

    private void startNight() {
        // Notifier les loups de voter
        for (ClientHandler client : clients) {
            if (client.getRole() == Role.LOUP && client.isAlive()) {
                client.send("MESSAGE C'est la phase nuit ! Les loups votent. Envoyez KILL <pseudo> pour voter.");
            }
        }
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

    public Phase getPhase() {
        return currentPhase;
    }

    public synchronized void registerKillVote(ClientHandler voter, String target) {

        ClientHandler victim = findPlayerbyPseudo(target);
        if(victim == null || !victim.isAlive()) {
            voter.send("MESSAGE Cible invalide pour le vote");
            return;
        }

        killVotes.put(target, killVotes.getOrDefault(target, 0) + 1);
        broadcast("MESSAGE Un loup à voté...");
        voter.setHasVoted(true);

        if(allWolvesHaveVoted()){
            resolveNight();
        }
    }

    private boolean allWolvesHaveVoted() {
        for(ClientHandler client : clients) {
            if(client.getRole() == Role.LOUP && client.isAlive() && !client.hasVoted()) {
                return false;
            }
        }
        return true;
    }

    private void resolveNight() {
        if(killVotes.isEmpty()) {
            broadcast("MESSAGE Aucun vote de loup enregistré. Aucun villageois n'a été tué cette nuit.");
        } else {
            String victim = getMaxVoted(killVotes);
            ClientHandler victimPlayer = findPlayerbyPseudo(victim);
            if(victimPlayer != null) {
                victimPlayer.kill();
                broadcast("MESSAGE " + victim + " a été tué pendant la nuit !");
            } else {
                broadcast("MESSAGE Aucun villageois n'a été tué cette nuit.");
            }
        }

        killVotes.clear();
        resetVotes();
        startDay();
    }

    private void resetVotes() {
        for(ClientHandler client : clients) {
            client.resetVote();
        }
    }

    private ClientHandler findPlayerbyPseudo(String pseudo) {
        for(ClientHandler client : clients) {
            if(client.getPseudo() != null && client.getPseudo().equalsIgnoreCase(pseudo)) {
                return client;
            }
        }
        return null;
    }

    private void startDay() {
        currentPhase = Phase.JOUR;
        broadcast("PHASE JOUR");
        broadcast("MESSAGE Le jour se lève...");
    }

    private String getMaxVoted(Map<String, Integer> votes) {
        int max = Collections.max(votes.values());
        List<String> top = new ArrayList<>();
        for (var e : votes.entrySet()) {
            if (e.getValue() == max) {
                top.add(e.getKey());
            }
        }
        return top.get(new Random().nextInt(top.size()));
    }

    public boolean isAdmin(ClientHandler client) {
        return adminClient != null && adminClient.equals(client);
    }

    public ClientHandler getAdmin() {
        return adminClient;
    }

    public static void main(String[] args) throws Exception {
        GameServer server = new GameServer();
        server.start();
    }
}
