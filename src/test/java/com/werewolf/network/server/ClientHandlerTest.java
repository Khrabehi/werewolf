package com.werewolf.network.server;

import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.werewolf.network.shared.Message;
import com.werewolf.network.shared.MessageType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientHandlerTest {
    
    @Test
    void testPingReceivesPong() throws Exception {
        try (ServerSocket testServer = new ServerSocket(0)) {
            int port = testServer.getLocalPort();

            // Start the client handler thread
            Thread handlerThread = new Thread(() -> {
                try (Socket serverSideSocket = testServer.accept()) {
                    ClientHandler handler = new ClientHandler(serverSideSocket);
                    handler.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            handlerThread.start();

            try (Socket testClient = new Socket("localhost", port);
                 ObjectOutputStream out = new ObjectOutputStream(testClient.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(testClient.getInputStream())) {

                Message pingMessage = new Message(MessageType.PING, "Test runner", "Ping !");
                out.writeObject(pingMessage);
                out.flush();

                Message response = (Message) in.readObject();

                assertNotNull(response);
                assertEquals(MessageType.PONG, response.getType());
                assertEquals("Server", response.getSender());
            }

            handlerThread.interrupt();
        }

    }
}
