package domain.model;

import domain.role.Role;
import java.util.UUID;

/**
 * Player Entity - SRP Principle (Single Responsibility)
 * Represents only a player's state
 */
public class Player {
    private final String id;
    private String pseudo;
    private Role role;
    private boolean alive;
    private boolean hasVoted;

    public Player(String pseudo) {
        this.id = UUID.randomUUID().toString();
        this.pseudo = pseudo;
        this.alive = true;
        this.hasVoted = false;
    }

    public String getId() {
        return id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public Role getRole() {
        return role;
    }

    public void assignRole(Role role) {
        this.role = role;
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        this.alive = false;
    }

    public boolean hasVoted() {
        return hasVoted;
    }

    public void vote() {
        this.hasVoted = true;
    }

    public void resetVote() {
        this.hasVoted = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Player{" +
                "pseudo='" + pseudo + '\'' +
                ", role=" + role +
                ", alive=" + alive +
                '}';
    }
}
