package com.werewolf.game;

import com.werewolf.game.role.PlayerRole;

public class Player {
    private String id;
    private String username; 
    private PlayerRole role;
    private boolean isAlive;
    private boolean isProtected;

    public Player(String id, String username) {
        this.id = id;
        this.username = username;
        this.isAlive = true; 
        this.isProtected = false; 
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }
}
