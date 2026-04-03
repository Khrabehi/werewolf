package com.werewolf.game;

import java.io.Serializable;

public class GameStateUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String updateMessage;

    public GameStateUpdate(String updateMessage) {
        this.updateMessage = updateMessage;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }
}