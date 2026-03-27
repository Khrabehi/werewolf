package com.werewolf.network.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.werewolf.network.shared.Message;
import com.werewolf.network.shared.MessageType;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("New client : " + socket.getInetAddress());

            // Infinite loop to listen to client
            while (true) {
                Message receivedMessage = (Message) in.readObject();
                System.out.println("Serveur a reçu : " + receivedMessage);

                // Ping/Pong logic
                if (receivedMessage.getType() == MessageType.PING) {
                    Message pongMessage = new Message(MessageType.PONG, "Server", "Return pong");
                    out.writeObject(pongMessage);
                    out.flush();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Déconnexion du client " + socket.getInetAddress());
        } finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        try{
            if(in!=null){
                in.close();
            }
            if(out != null) {
                out.close();
            }
            if(socket != null) {
                socket.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
