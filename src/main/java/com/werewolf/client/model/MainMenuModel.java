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

    private final List<PropertyChangeListener> listeners = new ArrayList<>();

    public MainMenuModel() {
        this.ipAddress = "127.0.0.1";
        this.port = "8443";
        this.statusMessage = "Ready to connect";
        this.isConnecting = false;
    }

    public void setUsername(String username) {
        String old = this.username;
        this.username = username;
        notifyListeners("username", old, username);
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
