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
    private GameState currentPhase;
    private Map<String, String> currentVotes = new ConcurrentHashMap<>();
    private final List<String> joinOrder = new CopyOnWriteArrayList<>();
    private String adminId;
    
    private final List<GameStateObserver> observers = new CopyOnWriteArrayList<>();

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.players = new ConcurrentHashMap<>();
        this.currentPhase = GameState.LOBBY;
    }

    public void addPlayer(Player player) {
        players.put(player.getId(), player);
        if (!joinOrder.contains(player.getId())) {
            joinOrder.add(player.getId());
        }
        assignAdminIfNeeded();
    }

    public Player getPlayer(String id) {
        return players.get(id);
    }

    public List<Player> getAlivePlayers() {
        return players.values().stream()
                .filter(Player::isAlive)
                .collect(Collectors.toList());
    }

    public List<Player> getPlayers() {
        return List.copyOf(players.values());
    }

    public void removePlayer(String playerId) {
        handlePlayerLeave(playerId);
    }

    public void handlePlayerLeave(String playerId) {
        Player removed = players.remove(playerId);
        joinOrder.remove(playerId);
        if (removed != null) {
            if (playerId.equals(adminId)) {
                adminId = null;
                assignAdminIfNeeded();
            }
            notifySessionUpdate("Player " + removed.getUsername() + " has left the game.");
        }
    }

    public void assignAdminIfNeeded() {
        if (adminId != null) {
            return;
        }
        if (!joinOrder.isEmpty()) {
            adminId = joinOrder.get(0);
        }
    }

    public List<String> getPlayerNames() {
        return joinOrder.stream()
            .map(players::get)
            .filter(p -> p != null)
            .map(Player::getUsername)
            .collect(Collectors.toList());
    }

    public void subscribe(GameStateObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(GameStateObserver observer) {
        observers.remove(observer);
    }

    public void notifySessionUpdate(String message) {
        notifySessionUpdate(message, null);
    }

    public void notifySessionUpdate(String message, Map<String, Object> metadata) {
        GameStateUpdate update = new GameStateUpdate(
            message,
            this.currentPhase,
            getAlivePlayers(),
            metadata
        );

        observers.forEach(observer -> observer.onGameStateUpdate(update));
    }

    // --- Logique de Jeu ---
    public void updatePhase(GameState phase) {
        updatePhase(phase, "Phase updated to: " + phase, null);
    }

    public void updatePhase(GameState phase, String message) {
        updatePhase(phase, message, null);
    }

    public void updatePhase(GameState phase, String message, Map<String, Object> metadata) {
        this.currentPhase = phase;
        if (phase == GameState.NIGHT) {
            resetProtections();
        }
        notifySessionUpdate(message, metadata);
    }

    private void resetProtections() {
        players.values().forEach(p -> p.setProtected(false));
    }

    public void recordVote(String voterId, String targetId) {
        currentVotes.put(voterId, targetId);
    }

    public Map<String, String> getCurrentVotes() {
        return new ConcurrentHashMap<>(currentVotes);
    }

    public void resetVotes() {
        currentVotes.clear();
    }

    public void sendPrivateMessage(String playerId, String message) {
        System.out.println("[PRIVATE to " + playerId + "] " + message);
    }

    public GameState getCurrentPhase() { return currentPhase; }
    public String getSessionId() { return sessionId; }
    public String getAdminId() { return adminId; }

    public long countAliveWerewolves() {
        return getAlivePlayers().stream()
            .filter(p -> p.getRole() != null && "Werewolf".equals(p.getRole().getName()))
            .count();
    }

    public long countAliveVillagers() {
        long alive = getAlivePlayers().size();
        return alive - countAliveWerewolves();
    }
}