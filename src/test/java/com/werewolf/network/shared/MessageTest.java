package com.werewolf.network.shared;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageTest {
    
    @Test
    void testMessageSerialization() throws IOException, ClassNotFoundException {
        Message originalMessage = new Message(MessageType.PING, "Test user", "Hello world!");

        byte[] serialized;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(originalMessage);
            oos.flush();
            serialized = baos.toByteArray();
        }

        Message deserializedMessage;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserializedMessage = (Message) ois.readObject();
        }

        assertNotNull(deserializedMessage);
        assertEquals(MessageType.PING, deserializedMessage.getType());
        assertEquals("Test user", deserializedMessage.getSender());
        assertEquals("Hello world!", deserializedMessage.getContent());

    }
}
