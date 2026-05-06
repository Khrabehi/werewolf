package com.werewolf.client.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class MainMenuModel {
    private String username;
    private String ipAddress;
    private String port;
    private String statusMessage;
    private boolean isConnecting;
    private List<String> playerNames;
    private String adminName;
    private boolean isAdmin;
    private boolean gameStarted;

    private final List<PropertyChangeListener> listeners = new ArrayList<>();

    public MainMenuModel() {
        this.ipAddress = "127.0.0.1";
        this.port = "8443";
        this.statusMessage = "Ready to connect";
        this.isConnecting = false;
        this.playerNames = new ArrayList<>();
        this.adminName = null;
        this.isAdmin = false;
        this.gameStarted = false;
    }

    public void setUsername(String username) {
        String old = this.username;
        this.username = username;
        notifyListeners("username", old, username);
        updateAdminStatus();
    }

    public void setIpAddress(String ipAddress) {
        String old = this.ipAddress;
        this.ipAddress = ipAddress;
        notifyListeners("ipAddress", old, ipAddress);
    }

    public void setPort(String port) {
        String old = this.port;
        this.port = port;
        notifyListeners("port", old, port);
    }

    public void setStatusMessage(String message) {
        String old = this.statusMessage;
        this.statusMessage = message;
        notifyListeners("statusMessage", old, message);
    }

    public void setIsConnecting(boolean connecting) {
        boolean old = this.isConnecting;
        this.isConnecting = connecting;
        notifyListeners("isConnecting", old, connecting);
    }

    public void setPlayerNames(List<String> playerNames) {
        List<String> old = this.playerNames;
        this.playerNames = (playerNames != null) ? new ArrayList<>(playerNames) : new ArrayList<>();
        notifyListeners("playerNames", old, this.playerNames);
    }

    public void setAdminName(String adminName) {
        String old = this.adminName;
        this.adminName = adminName;
        notifyListeners("adminName", old, adminName);
        updateAdminStatus();
    }

    public void setIsAdmin(boolean isAdmin) {
        boolean old = this.isAdmin;
        this.isAdmin = isAdmin;
        notifyListeners("isAdmin", old, isAdmin);
    }

    public void setGameStarted(boolean started) {
        if (this.gameStarted == started) {
            return;
        }
        boolean old = this.gameStarted;
        this.gameStarted = started;
        notifyListeners("gameStarted", old, started);
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getPort() {
        return port;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public List<String> getPlayerNames() {
        return new ArrayList<>(playerNames);
    }

    public String getAdminName() {
        return adminName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isGameStarted() {
        return gameStarted;
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

    private void updateAdminStatus() {
        if (username == null || adminName == null) {
            setIsAdmin(false);
            return;
        }
        setIsAdmin(username.equals(adminName));
    }
}
