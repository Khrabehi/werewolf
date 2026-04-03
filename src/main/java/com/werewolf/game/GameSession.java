package com.werewolf.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameSession {
    private String sessionId;
    private Map<String, Player> players;
    private GamePhase currentPhase;
    private Map<String, String> currentVotes = new ConcurrentHashMap<>();

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

    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public List<Player> getAlivePlayers() {
        return players.values().stream()
                .filter(Player::isAlive)
                .collect(Collectors.toList());
    }

    public String getSessionId() {
        return sessionId;
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void updatePhase(GamePhase phase) {
        this.currentPhase = phase;

        if (phase == GamePhase.NIGHT) {
            resetProtections();
        }
    }

    private void resetProtections() {
        for (Player player : players.values()) {
            player.setProtected(false);
        }
    }

    public void recordVote(String voterId, String targetId) {
        currentVotes.put(voterId, targetId);
    }

    public void notifySessionUpdate(String message) {
        System.out.println("[BROADCAST] " + message);
    }

    public void sendPrivateMessage(String playerId, String message) {
        System.out.println("[PRIVATE to " + playerId + "] " + message);
    }
}
