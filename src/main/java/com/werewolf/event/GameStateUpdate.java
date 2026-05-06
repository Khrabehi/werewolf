package com.werewolf.event;

import com.werewolf.game.GameState;
import com.werewolf.game.Player;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameStateUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private GameState newPhase;
    private List<Player> alivePlayers;
    private Map<String, Object> metadata;

    public GameStateUpdate(String message, GameState phase, List<Player> alive) {
        this.message = message;
        this.newPhase = phase;
        this.alivePlayers = alive;
        this.metadata = new HashMap<>(); // Initialisation pour éviter les NullPointerException
    }

    public GameStateUpdate(String message, GameState phase, List<Player> alive, Map<String, Object> metadata) {
        this.message = message;
        this.newPhase = phase;
        this.alivePlayers = alive;
        this.metadata = (metadata != null) ? new HashMap<>(metadata) : new HashMap<>();
    }

    public String getMessage() {
        return message;
    }

    public GameState getNewPhase() {
        return newPhase;
    }

    public List<Player> getAlivePlayers() {
        return alivePlayers;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}