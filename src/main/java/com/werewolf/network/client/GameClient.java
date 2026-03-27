package com.werewolf.network.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.werewolf.network.shared.Message;
import com.werewolf.network.shared.MessageType;

public class GameClient {

    private String serverAddress;
    private int serverPort;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String playerName;

    public GameClient(String serverAddress, int serverPort, String playerName) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.playerName = playerName;
    }

    public void connect() {
        try {
            socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server " + serverAddress + ":" + serverPort);

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Thread listenerThread = new Thread(this::listenForMessages);
            listenerThread.start();

            Message pingMessage = new Message(MessageType.PING, playerName, "Hello world!");
            sendMessage(pingMessage);
        } catch (IOException e) {
            System.err.println("Impossible to connect to the server : " + e.getMessage());
        }
    }

    public void sendMessage(Message message) {
        try {
            if (out != null) {
                out.writeObject(message);
                out.flush();
                System.out.println("-> Send : " + message.getType());
            }
        } catch (IOException e) {
            System.err.println("Error during the sending of a message : " + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            while (true) {
                Message receivedMessage = (Message) in.readObject();
                System.out.println("<- Received from server : " + receivedMessage);
            }
        }   catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection with the server lost");
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
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
            System.out.println("Disconnected properly.");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GameClient client = new GameClient("127.0.0.1", 8080, "Joueur1");
        client.connect();
    }
}
