package com.werewolf.client.model;

public class ConnectionConfig {
    private final String username;
    private final String ipAddress;
    private final int port;

    public ConnectionConfig(String username, String ipAddress, int port) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format(
            "ConnectionConfig{username='%s', ip='%s', port=%d}",
            username,
            ipAddress,
            port
        );
    }
}
