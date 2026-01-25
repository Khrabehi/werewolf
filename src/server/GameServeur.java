package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServeur {
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

    public static void main(String[] args) throws Exception {
        GameServeur server = new GameServeur();
        server.start();
    }
}
