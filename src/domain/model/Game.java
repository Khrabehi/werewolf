package domain.model;

import domain.phase.GamePhase;
import java.util.*;

/**
 * Game Aggregate - SRP Principle
 * Responsible for the global game state
 */
public class Game {
    private final String id;
    private final List<Player> players;
    private GamePhase currentPhase;
    private Player admin;
    private boolean started;
    private final int minPlayers;
    private final int maxPlayers;

    public Game(int minPlayers, int maxPlayers) {
        this.id = UUID.randomUUID().toString();
        this.players = new ArrayList<>();
        this.started = false;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public String getId() {
        return id;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Player> getAlivePlayers() {
        return players.stream()
                .filter(Player::isAlive)
                .toList();
    }

    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(GamePhase phase) {
        this.currentPhase = phase;
    }

    public Player getAdmin() {
        return admin;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        if (!canStart()) {
            throw new IllegalStateException("Cannot start game: not enough players");
        }
        this.started = true;
    }

    public boolean canStart() {
        return players.size() >= minPlayers &&
                players.size() <= maxPlayers &&
                players.stream().allMatch(p -> p.getPseudo() != null);
    }

    public void addPlayer(Player player) {
        if (started) {
            throw new IllegalStateException("Cannot add player: game already started");
        }
        if (players.size() >= maxPlayers) {
            throw new IllegalStateException("Cannot add player: game is full");
        }
        players.add(player);
        if (admin == null) {
            admin = player;
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);

        // If the admin left and there are still players, assign a new admin
        if (admin != null && admin.equals(player) && !players.isEmpty()) {
            admin = players.get(0);
            System.out.println("New admin assigned: " + admin.getPseudo());
        } else if (players.isEmpty()) {
            admin = null;
        }
    }

    public Optional<Player> findPlayerByPseudo(String pseudo) {
        return players.stream()
                .filter(p -> p.getPseudo() != null && p.getPseudo().equalsIgnoreCase(pseudo))
                .findFirst();
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void resetAllVotes() {
        players.forEach(Player::resetVote);
    }
}
