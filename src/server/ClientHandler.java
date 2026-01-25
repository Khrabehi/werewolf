package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameServeur server;

    public ClientHandler(Socket socket, GameServeur server) throws IOException {
        this.socket = socket;
        this.server = server;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try{
            out.println("WELCOME");
            String line; 
            while ((line = in.readLine()) != null) {
                System.out.println("Client: " + line);
            }
        }catch(IOException e){
            System.err.println("Erreur de communication avec le client : " + e.getMessage());
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