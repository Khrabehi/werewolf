package com.werewolf.network.server;

import com.werewolf.network.shared.Message;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gère la connexion et l'envoie de message des clients
 */
public class PlayerConnectionManager {
    private static final Map<String, ClientHandler> activeConnections = new ConcurrentHashMap<>();

    // Enregistre une nouvelle connexion de joueur
    public static void registerConnection(String playerId, ClientHandler handler) {
        activeConnections.put(playerId, handler);
        System.out.println("Registered connection for player: " + playerId + 
                          " (Total connections: " + activeConnections.size() + ")");
    }

    // Supprime une connexion de joueur
    public static void unregisterConnection(String playerId) {
        activeConnections.remove(playerId);
        System.out.println("Unregistered connection for player: " + playerId + 
                          " (Total connections: " + activeConnections.size() + ")");
    }

    // Broadcast un message à tous les clients connectés
    public static void broadcastToAll(Message message) {
        activeConnections.values().forEach(handler -> {
            try {
                handler.sendMessage(message);
            } catch (IOException e) {
                System.err.println("Failed to broadcast to client: " + e.getMessage());
            }
        });
    }

    // Broadcast un message à tous les clients sauf un
    public static void broadcastExcept(String excludePlayerId, Message message) {
        activeConnections.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(excludePlayerId))
            .forEach(entry -> {
                try {
                    entry.getValue().sendMessage(message);
                } catch (IOException e) {
                    System.err.println("Failed to broadcast to client " + entry.getKey() + ": " + e.getMessage());
                }
            });
    }

    // Récupère le nombre de connexions actives
    public static int getActiveConnectionCount() {
        return activeConnections.size();
    }
}
