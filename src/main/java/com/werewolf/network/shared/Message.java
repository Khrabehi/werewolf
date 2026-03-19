package com.werewolf.network.shared;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionId = 1L;

    private MessageType type;
    private String sender;
    private Object content;

    public Message(MessageType type, String sender, Object content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    public MessageType getType() { return type; }
    public String getSender() { return sender; }
    public Object getContent() { return content; }

    @Override
    public String toString() {
        return "[" + sender + "] " + type + ": " + content;
    }
}