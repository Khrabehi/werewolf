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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalMessage);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Message deserializedMessage = (Message) ois.readObject();
        ois.close();

        assertNotNull(deserializedMessage);
        assertEquals(MessageType.PING, deserializedMessage.getType());
        assertEquals("Test user", deserializedMessage.getSender());
        assertEquals("Hello world!", deserializedMessage.getContent());

    }
}
