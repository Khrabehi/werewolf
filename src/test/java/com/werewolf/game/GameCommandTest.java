package com.werewolf.game;

import com.werewolf.network.shared.GameCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameCommand Tests")
public class GameCommandTest {
    
    private GameCommand command;
    
    @Test
    @DisplayName("GameCommand can be created with basic parameters")
    public void testGameCommandCreation() {
        command = new GameCommand("KILL", "player2");
        assertEquals("KILL", command.getActionType());
        assertEquals("player2", command.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("GameCommand creates empty metadata by default")
    public void testGameCommandDefaultMetadata() {
        command = new GameCommand("KILL", "player2");
        assertNotNull(command.getMetadata());
        assertTrue(command.getMetadata().isEmpty());
    }
    
    @Test
    @DisplayName("GameCommand metadata can be populated via addMetadata")
    public void testGameCommandAddMetadata() {
        command = new GameCommand("VOTE", "player3");
        command.addMetadata("reason", "suspicious");
        
        assertEquals("VOTE", command.getActionType());
        assertEquals("player3", command.getTargetPlayerId());
        assertEquals("suspicious", command.getMetadata().get("reason"));
    }
    
    @Test
    @DisplayName("GameCommand supports KILL action")
    public void testKillCommand() {
        command = new GameCommand("KILL", "victim");
        assertEquals("KILL", command.getActionType());
        assertEquals("victim", command.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("GameCommand supports VOTE action")
    public void testVoteCommand() {
        command = new GameCommand("VOTE", "suspect");
        assertEquals("VOTE", command.getActionType());
        assertEquals("suspect", command.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("GameCommand supports HEAL action")
    public void testHealCommand() {
        command = new GameCommand("HEAL", "ally");
        assertEquals("HEAL", command.getActionType());
        assertEquals("ally", command.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("GameCommand supports PEEK action")
    public void testPeekCommand() {
        command = new GameCommand("PEEK", "suspect");
        assertEquals("PEEK", command.getActionType());
        assertEquals("suspect", command.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("GameCommand can have any player ID as target")
    public void testVariousTargetPlayerIds() {
        GameCommand cmd1 = new GameCommand("KILL", "player1");
        GameCommand cmd2 = new GameCommand("KILL", "player123");
        GameCommand cmd3 = new GameCommand("KILL", "alice-2026");
        
        assertEquals("player1", cmd1.getTargetPlayerId());
        assertEquals("player123", cmd2.getTargetPlayerId());
        assertEquals("alice-2026", cmd3.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("GameCommand target can be null")
    public void testNullTargetPlayerId() {
        command = new GameCommand("CHAT", null);
        assertNull(command.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("GameCommand target can be empty string")
    public void testEmptyTargetPlayerId() {
        command = new GameCommand("CHAT", "");
        assertEquals("", command.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("GameCommand metadata can store multiple key-value pairs")
    public void testMetadataMultipleEntries() {
        command = new GameCommand("PEEK", "suspect");
        command.addMetadata("timestamp", System.currentTimeMillis());
        command.addMetadata("evidence", "night kill");
        command.addMetadata("confidence", 0.95);
        
        assertEquals(3, command.getMetadata().size());
        assertNotNull(command.getMetadata().get("timestamp"));
        assertEquals("night kill", command.getMetadata().get("evidence"));
    }
    
    @Test
    @DisplayName("GameCommand metadata can be replaced via setMetadata")
    public void testSetMetadata() {
        command = new GameCommand("KILL", "target");
        command.addMetadata("old", "value");
        
        Map<String, Object> newMetadata = new HashMap<>();
        newMetadata.put("new", "data");
        command.setMetadata(newMetadata);
        
        assertEquals(1, command.getMetadata().size());
        assertNull(command.getMetadata().get("old"));
        assertEquals("data", command.getMetadata().get("new"));
    }
    
    @Test
    @DisplayName("GameCommand metadata remains independent between commands")
    public void testMetadataIndependence() {
        GameCommand cmd1 = new GameCommand("KILL", "target");
        GameCommand cmd2 = new GameCommand("KILL", "target");
        
        cmd1.addMetadata("test", "value1");
        cmd2.addMetadata("test", "value2");
        
        assertEquals("value1", cmd1.getMetadata().get("test"));
        assertEquals("value2", cmd2.getMetadata().get("test"));
    }
    
    @Test
    @DisplayName("GameCommand is serializable")
    public void testGameCommandSerializable() {
        command = new GameCommand("KILL", "player2");
        assertTrue(command instanceof java.io.Serializable);
    }
    
    @Test
    @DisplayName("GameCommand with metadata is serializable")
    public void testGameCommandWithMetadataSerializable() {
        command = new GameCommand("VOTE", "player3");
        command.addMetadata("reason", "test");
        
        assertTrue(command instanceof java.io.Serializable);
    }
    
    @Test
    @DisplayName("GameCommand action type can be changed via setter")
    public void testSetActionType() {
        command = new GameCommand("KILL", "target");
        assertEquals("KILL", command.getActionType());
        
        command.setActionType("VOTE");
        assertEquals("VOTE", command.getActionType());
    }
    
    @Test
    @DisplayName("GameCommand target can be changed via setter")
    public void testSetTargetPlayerId() {
        command = new GameCommand("KILL", "player1");
        assertEquals("player1", command.getTargetPlayerId());
        
        command.setTargetPlayerId("player2");
        assertEquals("player2", command.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("Multiple GameCommand instances are independent")
    public void testMultipleCommandsIndependent() {
        GameCommand cmd1 = new GameCommand("KILL", "player1");
        GameCommand cmd2 = new GameCommand("VOTE", "player2");
        GameCommand cmd3 = new GameCommand("HEAL", "player3");
        
        assertEquals("KILL", cmd1.getActionType());
        assertEquals("VOTE", cmd2.getActionType());
        assertEquals("HEAL", cmd3.getActionType());
        
        assertEquals("player1", cmd1.getTargetPlayerId());
        assertEquals("player2", cmd2.getTargetPlayerId());
        assertEquals("player3", cmd3.getTargetPlayerId());
    }
    
    @Test
    @DisplayName("Command sequence allows different actions")
    public void testCommandSequence() {
        GameCommand killCmd = new GameCommand("KILL", "victim");
        GameCommand voteCmd = new GameCommand("VOTE", "suspect");
        GameCommand healCmd = new GameCommand("HEAL", "ally");
        
        assertEquals("KILL", killCmd.getActionType());
        assertEquals("VOTE", voteCmd.getActionType());
        assertEquals("HEAL", healCmd.getActionType());
    }
    
    @Test
    @DisplayName("GameCommand with special characters in action type")
    public void testSpecialCharactersActionType() {
        command = new GameCommand("KILL_SILENTLY", "target");
        assertEquals("KILL_SILENTLY", command.getActionType());
    }
    
    @Test
    @DisplayName("GameCommand with complex metadata objects")
    public void testComplexMetadata() {
        command = new GameCommand("INVESTIGATE", "target");
        
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("nested", "value");
        command.addMetadata("complex", innerMap);
        
        assertNotNull(command.getMetadata().get("complex"));
    }
    
    @Test
    @DisplayName("GameCommand metadata can handle null values")
    public void testMetadataNullValues() {
        command = new GameCommand("VOTE", "target");
        command.addMetadata("nullable", null);
        
        assertNull(command.getMetadata().get("nullable"));
    }
    
    @Test
    @DisplayName("GameCommand toString works")
    public void testToString() {
        command = new GameCommand("KILL", "victim");
        assertNotNull(command.toString());
        assertTrue(command.toString().length() > 0);
    }
}
