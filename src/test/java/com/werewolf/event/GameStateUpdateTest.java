package com.werewolf.event;

import com.werewolf.game.GamePhase;
import com.werewolf.game.Player;
import com.werewolf.game.role.VillagerRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameStateUpdate Tests")
public class GameStateUpdateTest {
    
    private GameStateUpdate update;
    private GamePhase testPhase;
    private List<Player> testPlayers;
    private Map<String, Object> metadata;
    
    @BeforeEach
    public void setUp() {
        testPhase = GamePhase.NIGHT;
        testPlayers = new ArrayList<>();
        Player p1 = new Player("p1", "Alice");
        p1.setRole(new VillagerRole());
        testPlayers.add(p1);
        Player p2 = new Player("p2", "Bob");
        p2.setRole(new VillagerRole());
        testPlayers.add(p2);
        
        metadata = new HashMap<>();
    }
    
    // ============== BASIC CREATION TESTS ==============
    
    @Test
    @DisplayName("GameStateUpdate can be created with message")
    public void testGameStateUpdateCreation() {
        update = new GameStateUpdate("Game phase changed", testPhase, testPlayers);
        
        assertEquals("Game phase changed", update.getMessage());
        assertEquals(testPhase, update.getNewPhase());
        assertEquals(testPlayers, update.getAlivePlayers());
    }
    
    @Test
    @DisplayName("GameStateUpdate can be created with metadata")
    public void testGameStateUpdateWithMetadata() {
        update = new GameStateUpdate("Player eliminated", testPhase, testPlayers);
        update.addMetadata("reason", "death");
        
        assertEquals("Player eliminated", update.getMessage());
        assertEquals(testPhase, update.getNewPhase());
        assertEquals(testPlayers, update.getAlivePlayers());
    }
    
    @Test
    @DisplayName("GameStateUpdate without metadata")
    public void testGameStateUpdateWithoutMetadata() {
        update = new GameStateUpdate("Phase transition", testPhase, testPlayers);
        
        assertNotNull(update.getMessage());
        assertNotNull(update.getNewPhase());
        assertNotNull(update.getAlivePlayers());
    }
    
    // ============== MESSAGE TESTS ==============
    
    @Test
    @DisplayName("GameStateUpdate stores message correctly")
    public void testGetMessage() {
        String message = "Night phase started";
        update = new GameStateUpdate(message, testPhase, testPlayers);
        
        assertEquals(message, update.getMessage());
    }
    
    @Test
    @DisplayName("GameStateUpdate can have different messages")
    public void testDifferentMessages() {
        GameStateUpdate update1 = new GameStateUpdate("Player died", testPhase, testPlayers);
        GameStateUpdate update2 = new GameStateUpdate("Phase changed", testPhase, testPlayers);
        
        assertNotEquals(update1.getMessage(), update2.getMessage());
    }
    
    @Test
    @DisplayName("GameStateUpdate message can be empty")
    public void testEmptyMessage() {
        update = new GameStateUpdate("", testPhase, testPlayers);
        assertEquals("", update.getMessage());
    }
    
    @Test
    @DisplayName("GameStateUpdate message can be null")
    public void testNullMessage() {
        update = new GameStateUpdate(null, testPhase, testPlayers);
        assertNull(update.getMessage());
    }
    
    // ============== PHASE TESTS ==============
    
    @Test
    @DisplayName("GameStateUpdate stores phase correctly")
    public void testGetNewPhase() {
        update = new GameStateUpdate("Phase changed", GamePhase.DAY_VOTING, testPlayers);
        assertEquals(GamePhase.DAY_VOTING, update.getNewPhase());
    }
    
    @Test
    @DisplayName("GameStateUpdate can have different phases")
    public void testDifferentPhases() {
        GameStateUpdate nightUpdate = new GameStateUpdate("Night started", GamePhase.NIGHT, testPlayers);
        GameStateUpdate dayUpdate = new GameStateUpdate("Day started", GamePhase.DAY_DISCUSSION, testPlayers);
        
        assertNotEquals(nightUpdate.getNewPhase(), dayUpdate.getNewPhase());
        assertEquals(GamePhase.NIGHT, nightUpdate.getNewPhase());
        assertEquals(GamePhase.DAY_DISCUSSION, dayUpdate.getNewPhase());
    }
    
    @Test
    @DisplayName("GameStateUpdate phase can be null")
    public void testNullPhase() {
        update = new GameStateUpdate("Update", null, testPlayers);
        assertNull(update.getNewPhase());
    }
    
    // ============== ALIVE PLAYERS TESTS ==============
    
    @Test
    @DisplayName("GameStateUpdate stores alive players list")
    public void testGetAlivePlayers() {
        update = new GameStateUpdate("Update", testPhase, testPlayers);
        
        assertEquals(testPlayers, update.getAlivePlayers());
        assertEquals(2, update.getAlivePlayers().size());
    }
    
    @Test
    @DisplayName("GameStateUpdate with empty players list")
    public void testEmptyPlayersList() {
        List<Player> emptyList = new ArrayList<>();
        update = new GameStateUpdate("Game over", GamePhase.GAME_OVER, emptyList);
        
        assertTrue(update.getAlivePlayers().isEmpty());
    }
    
    @Test
    @DisplayName("GameStateUpdate with single player")
    public void testSinglePlayer() {
        List<Player> singlePlayer = new ArrayList<>();
        Player p = new Player("p1", "Alice");
        p.setRole(new VillagerRole());
        singlePlayer.add(p);
        
        update = new GameStateUpdate("Last player alive", testPhase, singlePlayer);
        
        assertEquals(1, update.getAlivePlayers().size());
        assertEquals("p1", update.getAlivePlayers().get(0).getId());
    }
    
    @Test
    @DisplayName("GameStateUpdate alive players can be null")
    public void testNullPlayersList() {
        update = new GameStateUpdate("Update", testPhase, null);
        assertNull(update.getAlivePlayers());
    }
    
    // ============== METADATA TESTS ==============
    
    @Test
    @DisplayName("GameStateUpdate with metadata")
    public void testGetMetadata() {
        update = new GameStateUpdate("Death notification", testPhase, testPlayers);
        update.addMetadata("killed_player", "player2");
        update.addMetadata("reason", "werewolf attack");
        
        assertNotNull(update.getMetadata());
        assertEquals("player2", update.getMetadata().get("killed_player"));
        assertEquals("werewolf attack", update.getMetadata().get("reason"));
    }
    
    @Test
    @DisplayName("GameStateUpdate with empty metadata")
    public void testEmptyMetadata() {
        update = new GameStateUpdate("Update", testPhase, testPlayers);
        
        assertNotNull(update.getMetadata());
        assertTrue(update.getMetadata().isEmpty());
    }
    
    @Test
    @DisplayName("GameStateUpdate without metadata argument")
    public void testNoMetadataProvided() {
        update = new GameStateUpdate("Update", testPhase, testPlayers);
        // Metadata should either be null or empty, not required
    }
    
    // ============== SERIALIZATION TESTS ==============
    
    @Test
    @DisplayName("GameStateUpdate is serializable")
    public void testGameStateUpdateSerializable() {
        update = new GameStateUpdate("Update", testPhase, testPlayers);
        
        assertTrue(update instanceof java.io.Serializable);
    }
    
    @Test
    @DisplayName("GameStateUpdate with complex metadata is serializable")
    public void testSerializableWithMetadata() {
        update = new GameStateUpdate("Update", testPhase, testPlayers);
        update.addMetadata("complex", new ArrayList<>());
        
        assertTrue(update instanceof java.io.Serializable);
    }
    
    // ============== EVENT SCENARIOS ==============
    
    @Test
    @DisplayName("Player death update")
    public void testPlayerDeathUpdate() {
        List<Player> alivePlayers = new ArrayList<>(testPlayers);
        alivePlayers.remove(0); // One player dies
        
        update = new GameStateUpdate(
            "Player p1 has been killed",
            GamePhase.DAY_DISCUSSION,
            alivePlayers
        );
        update.addMetadata("killed_player", "p1");
        update.addMetadata("killer_role", "Werewolf");
        
        assertEquals(1, update.getAlivePlayers().size());
        assertEquals("p1", update.getMetadata().get("killed_player"));
    }
    
    @Test
    @DisplayName("Phase transition update")
    public void testPhaseTransitionUpdate() {
        update = new GameStateUpdate("Transitioning to day voting", GamePhase.DAY_VOTING, testPlayers);
        
        assertEquals(GamePhase.DAY_VOTING, update.getNewPhase());
        assertEquals(2, update.getAlivePlayers().size());
    }
    
    @Test
    @DisplayName("Protection notification update")
    public void testProtectionUpdate() {
        update = new GameStateUpdate(
            "Player p2 is protected",
            GamePhase.NIGHT,
            testPlayers
        );
        update.addMetadata("protected_player", "p2");
        update.addMetadata("protector_role", "Medic");
        
        assertEquals("p2", update.getMetadata().get("protected_player"));
    }
    
    // ============== MULTIPLE UPDATES TESTS ==============
    
    @Test
    @DisplayName("Multiple GameStateUpdates are independent")
    public void testMultipleUpdatesIndependent() {
        GameStateUpdate update1 = new GameStateUpdate("Update 1", GamePhase.NIGHT, testPlayers);
        GameStateUpdate update2 = new GameStateUpdate("Update 2", GamePhase.DAY_VOTING, testPlayers);
        
        assertNotEquals(update1.getMessage(), update2.getMessage());
        assertNotEquals(update1.getNewPhase(), update2.getNewPhase());
    }
    
    // ============== EDGE CASES ==============
    
    @Test
    @DisplayName("GameStateUpdate with very large player list")
    public void testLargePlayerList() {
        List<Player> largePlayers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Player p = new Player("p" + i, "Player" + i);
            p.setRole(new VillagerRole());
            largePlayers.add(p);
        }
        
        update = new GameStateUpdate("Large game update", testPhase, largePlayers);
        
        assertEquals(100, update.getAlivePlayers().size());
    }
    
    @Test
    @DisplayName("GameStateUpdate with special characters in message")
    public void testSpecialCharactersMessage() {
        String message = "Update: @#$%^&*()_+-=[]{}|;':\",.<>?";
        update = new GameStateUpdate(message, testPhase, testPlayers);
        
        assertEquals(message, update.getMessage());
    }
}
