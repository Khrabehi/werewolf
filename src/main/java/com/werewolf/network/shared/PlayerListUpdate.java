package com.werewolf.network.shared;

import java.io.Serializable;
import java.util.List;

public class PlayerListUpdate implements Serializable{
    private final List<String> playerNames;
    private final String adminName;

    public PlayerListUpdate(List<String> playerNames, String adminName) {
        this.playerNames = playerNames;
        this.adminName = adminName;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public String getAdminName() {
        return adminName;
    }
}
