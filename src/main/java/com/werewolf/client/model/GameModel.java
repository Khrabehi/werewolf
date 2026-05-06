package com.werewolf.client.model;

import com.werewolf.game.GameState;
import com.werewolf.game.Player;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameModel {
    private final String myUsername;
    private String myRole;
    private GameState gamePhase;
    private List<Player> alivePlayers;
    private final List<String> eventLog;
    private boolean canAct;
    private boolean hasActedThisPhase;
    private String gameOverWinner;
    private Map<String, String> allRoles;

    private final List<PropertyChangeListener> listeners = new ArrayList<>();

    public GameModel(String myUsername) {
        this.myUsername = myUsername;
        this.myRole = null;
        this.gamePhase = GameState.LOBBY;
        this.alivePlayers = new ArrayList<>();
        this.eventLog = new ArrayList<>();
        this.canAct = false;
        this.hasActedThisPhase = false;
        this.allRoles = new HashMap<>();
    }

    public void setMyRole(String role) {
        String old = this.myRole;
        this.myRole = role;
        notifyListeners("myRole", old, role);
    }

    public void setGamePhase(GameState phase) {
        GameState old = this.gamePhase;
        this.gamePhase = phase;
        notifyListeners("gamePhase", old, phase);
    }

    public void setAlivePlayers(List<Player> players) {
        List<Player> old = this.alivePlayers;
        this.alivePlayers = new ArrayList<>(players);
        notifyListeners("alivePlayers", old, this.alivePlayers);
    }

    public void addEventLog(String message) {
        this.eventLog.add(message);
        notifyListeners("eventLog", null, message);
    }

    public void setCanAct(boolean canAct) {
        boolean old = this.canAct;
        this.canAct = canAct;
        notifyListeners("canAct", old, canAct);
    }

    public void setHasActedThisPhase(boolean hasActed) {
        boolean old = this.hasActedThisPhase;
        this.hasActedThisPhase = hasActed;
        notifyListeners("hasActedThisPhase", old, hasActed);
    }

    public void setGameOverWinner(String winner) {
        String old = this.gameOverWinner;
        this.gameOverWinner = winner;
        notifyListeners("gameOverWinner", old, winner);
    }

    @SuppressWarnings("unchecked")
    public void setAllRoles(Map<String, String> roles) {
        this.allRoles = new HashMap<>(roles);
        notifyListeners("allRoles", null, this.allRoles);
    }

    public String getMyUsername() { return myUsername; }
    public String getMyRole() { return myRole; }
    public GameState getGamePhase() { return gamePhase; }
    public List<Player> getAlivePlayers() { return new ArrayList<>(alivePlayers); }
    public List<String> getEventLog() { return new ArrayList<>(eventLog); }
    public boolean isCanAct() { return canAct; }
    public boolean isHasActedThisPhase() { return hasActedThisPhase; }
    public String getGameOverWinner() { return gameOverWinner; }
    public Map<String, String> getAllRoles() { return new HashMap<>(allRoles); }

    /**
     * Trouve l'identifiant attribué par le serveur au joueur (par ex. "Player-43210")
     * à partir du nom d'utilisateur affiché.
     */
    public String findPlayerIdByUsername(String username) {
        return alivePlayers.stream()
            .filter(p -> p.getUsername().equals(username))
            .map(Player::getId)
            .findFirst()
            .orElse(null);
    }

    /**
     * Retourne les noms d'utilisateur des joueurs encore en vie,
     * en excluant éventuellement le nom de ce client.
     */
    public List<String> getAlivePlayerUsernames(boolean excludeSelf) {
        return alivePlayers.stream()
            .map(Player::getUsername)
            .filter(name -> !excludeSelf || !name.equals(myUsername))
            .collect(Collectors.toList());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String property, Object oldValue, Object newValue) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldValue, newValue);
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(event);
        }
    }
}
