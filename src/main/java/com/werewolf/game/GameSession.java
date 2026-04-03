package com.werewolf.game;

import com.werewolf.event.GameStateObserver;
import com.werewolf.event.GameStateUpdate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class GameSession {
    private String sessionId;
    private Map<String, Player> players;
    private GamePhase currentPhase;
    private Map<String, String> currentVotes = new ConcurrentHashMap<>();
    
    private final List<GameStateObserver> observers = new CopyOnWriteArrayList<>();

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.players = new ConcurrentHashMap<>();
        this.currentPhase = GamePhase.LOBBY;
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public List<Player> getAlivePlayers() {
        return players.values().stream()
                .filter(Player::isAlive)
                .collect(Collectors.toList());
    }

    public void subscribe(GameStateObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(GameStateObserver observer) {
        observers.remove(observer);
    }

    public void notifySessionUpdate(String message) {
        GameStateUpdate update = new GameStateUpdate(
            message,
            this.currentPhase,
            getAlivePlayers()
        );
        
        observers.forEach(observer -> observer.onGameStateUpdate(update));
    }

    // --- Logique de Jeu ---
    public void updatePhase(GamePhase phase) {
        this.currentPhase = phase;
        if (phase == GamePhase.NIGHT) {
            resetProtections();
        }
        notifySessionUpdate("Phase updated to: " + phase);
    }

    private void resetProtections() {
        players.values().forEach(p -> p.setProtected(false));
    }

    public void recordVote(String voterId, String targetId) {
        currentVotes.put(voterId, targetId);
    }

    public void sendPrivateMessage(String playerId, String message) {
        System.out.println("[PRIVATE to " + playerId + "] " + message);
    }

    public GamePhase getCurrentPhase() { return currentPhase; }
    public String getSessionId() { return sessionId; }
}