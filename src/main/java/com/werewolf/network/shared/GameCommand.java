package com.werewolf.network.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameCommand implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String actionType;
    private String targetPlayerId;
    private Map<String, Object> metadata;
    
    public GameCommand(String actionType, String targetPlayerId) {
        this.actionType = actionType;
        this.targetPlayerId = targetPlayerId;
        this.metadata = new HashMap<>();
    }

    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getTargetPlayerId() {
        return targetPlayerId;
    }

    public void setTargetPlayerId(String targetPlayerId) {
        this.targetPlayerId = targetPlayerId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "GameCommand{" +
                "actionType='" + actionType + '\'' +
                ", targetPlayer='" + targetPlayerId + '\'' +
                (metadata.isEmpty() ? "" : ", metadata=" + metadata) +
                '}';
    }
}
