package com.werewolf.game;

import com.werewolf.event.GameStateObserver;
import com.werewolf.event.GameStateUpdate;
import com.werewolf.game.role.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameSession Tests")
public class GameSessionTest {
    
    private GameSession session;
    private Player player1;
    private Player player2;
    private Player player3;
    
    @BeforeEach
    public void setUp() {
        session = new GameSession("test-session");
        
        player1 = new Player("p1", "Player1");
        player1.setRole(new VillagerRole());
        
        player2 = new Player("p2", "Player2");
        player2.setRole(new WerewolfRole());
        
        player3 = new Player("p3", "Player3");
        player3.setRole(new MedicRole());
        
        session.addPlayer(player1);
        session.addPlayer(player2);
        session.addPlayer(player3);
    }
    
    // ============== PLAYER MANAGEMENT TESTS ==============
    
    @Test
    @DisplayName("Can add player to session")
    public void testAddPlayer() {
        Player newPlayer = new Player("p4", "NewPlayer");
        session.addPlayer(newPlayer);
        
        assertEquals(newPlayer, session.getPlayer("p4"));
    }
    
    @Test
    @DisplayName("Can retrieve player by ID")
    public void testGetPlayer() {
        assertEquals(player1, session.getPlayer("p1"));
        assertEquals(player2, session.getPlayer("p2"));
    }
    
    @Test
    @DisplayName("Returns null for non-existent player")
    public void testGetNonExistentPlayer() {
        assertNull(session.getPlayer("non-existent"));
    }
    
    @Test
    @DisplayName("Can remove player from session")
    public void testRemovePlayer() {
        session.removePlayer("p1");
        
        assertNull(session.getPlayer("p1"));
    }
    
    // ============== ALIVE PLAYERS TESTS ==============
    
    @Test
    @DisplayName("Get all alive players")
    public void testGetAlivePlayers() {
        List<Player> alive = session.getAlivePlayers();
        
        assertEquals(3, alive.size());
        assertTrue(alive.contains(player1));
        assertTrue(alive.contains(player2));
        assertTrue(alive.contains(player3));
    }
    
    @Test
    @DisplayName("Dead players not included in alive list")
    public void testDeadPlayersNotIncluded() {
        player1.setAlive(false);
        
        List<Player> alive = session.getAlivePlayers();
        
        assertEquals(2, alive.size());
        assertFalse(alive.contains(player1));
        assertTrue(alive.contains(player2));
        assertTrue(alive.contains(player3));
    }
    
    @Test
    @DisplayName("Empty alive list when all dead")
    public void testEmptyAliveWhenAllDead() {
        player1.setAlive(false);
        player2.setAlive(false);
        player3.setAlive(false);
        
        List<Player> alive = session.getAlivePlayers();
        
        assertTrue(alive.isEmpty());
    }
    
    // ============== ALL PLAYERS TESTS ==============
    
    @Test
    @DisplayName("Get all players returns immutable copy")
    public void testGetAllPlayersReturnsCopy() {
        List<Player> players = session.getPlayers();
        
        assertEquals(3, players.size());
        
        // Returned list should be immutable
        assertThrows(UnsupportedOperationException.class, () -> {
            players.clear();
        });
        
        // Session should still have all players
        assertEquals(3, session.getPlayers().size());
    }
    
    // ============== PHASE MANAGEMENT TESTS ==============
    
    @Test
    @DisplayName("Can update game phase")
    public void testUpdatePhase() {
        assertEquals(GamePhase.LOBBY, session.getCurrentPhase());
        
        session.updatePhase(GamePhase.NIGHT);
        assertEquals(GamePhase.NIGHT, session.getCurrentPhase());
        
        session.updatePhase(GamePhase.DAY_VOTING);
        assertEquals(GamePhase.DAY_VOTING, session.getCurrentPhase());
    }
    
    @Test
    @DisplayName("Phase update resets protections")
    public void testPhaseUpdateResetsProtections() {
        player1.setProtected(true);
        player2.setProtected(true);
        
        assertTrue(player1.isProtected());
        assertTrue(player2.isProtected());
        
        session.updatePhase(GamePhase.NIGHT);
        
        assertFalse(player1.isProtected(), "Protections should be reset on night phase");
        assertFalse(player2.isProtected());
    }
    
    // ============== VOTE MANAGEMENT TESTS ==============
    
    @Test
    @DisplayName("Can record vote")
    public void testRecordVote() {
        session.recordVote("p1", "p2");
        // Verify no exception thrown
        assertDoesNotThrow(() -> session.recordVote("p1", "p2"));
    }
    
    @Test
    @DisplayName("Can overwrite vote")
    public void testOverwriteVote() {
        session.recordVote("p1", "p2");
        session.recordVote("p1", "p3");
        
        // Both votes should execute without error
        assertDoesNotThrow(() -> session.recordVote("p1", "p2"));
    }
    
    // ============== OBSERVER PATTERN TESTS ==============
    
    @Test
    @DisplayName("Can subscribe observer")
    public void testSubscribeObserver() {
        List<GameStateUpdate> updates = new ArrayList<>();
        GameStateObserver observer = updates::add;
        
        session.subscribe(observer);
        session.notifySessionUpdate("Test message");
        
        assertEquals(1, updates.size());
    }
    
    @Test
    @DisplayName("Observer receives notification with message")
    public void testObserverReceivesMessage() {
        List<GameStateUpdate> updates = new ArrayList<>();
        session.subscribe(updates::add);
        
        session.notifySessionUpdate("Test message");
        
        assertEquals("Test message", updates.get(0).getMessage());
    }
    
    @Test
    @DisplayName("Observer receives notification with phase")
    public void testObserverReceivesPhase() {
        session.updatePhase(GamePhase.NIGHT);
        
        List<GameStateUpdate> updates = new ArrayList<>();
        session.subscribe(updates::add);
        
        session.notifySessionUpdate("Night phase started");
        
        assertEquals(GamePhase.NIGHT, updates.get(0).getNewPhase());
    }
    
    @Test
    @DisplayName("Observer receives current alive players")
    public void testObserverReceivesAlivePlayers() {
        player1.setAlive(false);
        
        List<GameStateUpdate> updates = new ArrayList<>();
        session.subscribe(updates::add);
        
        session.notifySessionUpdate("Alive players update");
        
        List<Player> alivePlayers = updates.get(0).getAlivePlayers();
        assertEquals(2, alivePlayers.size());
        assertFalse(alivePlayers.contains(player1));
    }
    
    @Test
    @DisplayName("Can unsubscribe observer")
    public void testUnsubscribeObserver() {
        List<GameStateUpdate> updates = new ArrayList<>();
        GameStateObserver observer = updates::add;
        
        session.subscribe(observer);
        session.unsubscribe(observer);
        session.notifySessionUpdate("Test message");
        
        assertTrue(updates.isEmpty(), "Unsubscribed observer should not receive updates");
    }
    
    @Test
    @DisplayName("Multiple observers receive notifications")
    public void testMultipleObservers() {
        List<GameStateUpdate> updates1 = new ArrayList<>();
        List<GameStateUpdate> updates2 = new ArrayList<>();
        
        session.subscribe(updates1::add);
        session.subscribe(updates2::add);
        
        session.notifySessionUpdate("Test message");
        
        assertEquals(1, updates1.size());
        assertEquals(1, updates2.size());
    }
    
    // ============== SESSION INFO TESTS ==============
    
    @Test
    @DisplayName("Session has unique ID")
    public void testSessionId() {
        GameSession session1 = new GameSession("session-1");
        GameSession session2 = new GameSession("session-2");
        
        assertEquals("session-1", session1.getSessionId());
        assertEquals("session-2", session2.getSessionId());
        assertNotEquals(session1.getSessionId(), session2.getSessionId());
    }
    
    @Test
    @DisplayName("Initial phase is LOBBY")
    public void testInitialPhaseIsLobby() {
        GameSession newSession = new GameSession("new-session");
        assertEquals(GamePhase.LOBBY, newSession.getCurrentPhase());
    }
}
