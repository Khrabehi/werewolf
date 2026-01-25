package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    private static final int PORT = 12345; 
    private List<ClientHandler> clients = new ArrayList<>();
    
    public void start() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT); 
        System.out.println("Serveur démarré sur le port " + PORT);

        while (true) {
            Socket client = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(client, this);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
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

    public static void main(String[] args) throws Exception {
        GameServer server = new GameServer();
        server.start();
    }
}
