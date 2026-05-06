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

@DisplayName("GameStateObserver Tests")
public class GameStateObserverTest {
    
    private GameStateUpdate testUpdate;
    private List<Player> testPlayers;
    private GamePhase testPhase;
    
    // Mock observer implementation for testing
    private static class MockGameStateObserver implements GameStateObserver {
        private GameStateUpdate lastUpdate;
        private int updateCount = 0;
        private boolean callbackReceived = false;
        
        @Override
        public void onGameStateUpdate(GameStateUpdate update) {
            this.lastUpdate = update;
            this.updateCount++;
            this.callbackReceived = true;
        }
        
        public GameStateUpdate getLastUpdate() {
            return lastUpdate;
        }
        
        public int getUpdateCount() {
            return updateCount;
        }
        
        public boolean hasReceivedCallback() {
            return callbackReceived;
        }
        
        public void reset() {
            lastUpdate = null;
            updateCount = 0;
            callbackReceived = false;
        }
    }
    
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
        
        testUpdate = new GameStateUpdate("Test update", testPhase, testPlayers);
    }
    
    // ============== INTERFACE IMPLEMENTATION TESTS ==============
    
    @Test
    @DisplayName("Observer can be created from interface")
    public void testObserverCreation() {
        GameStateObserver observer = new MockGameStateObserver();
        assertNotNull(observer);
    }
    
    @Test
    @DisplayName("Observer implements onGameStateUpdate method")
    public void testObserverMethod() {
        GameStateObserver observer = new MockGameStateObserver();
        assertTrue(observer instanceof GameStateObserver);
    }
    
    // ============== CALLBACK TESTS ==============
    
    @Test
    @DisplayName("Observer receives callback when update occurs")
    public void testObserverReceivesCallback() {
        MockGameStateObserver observer = new MockGameStateObserver();
        observer.onGameStateUpdate(testUpdate);
        
        assertTrue(observer.hasReceivedCallback());
    }
    
    @Test
    @DisplayName("Observer stores received update")
    public void testObserverStoresUpdate() {
        MockGameStateObserver observer = new MockGameStateObserver();
        observer.onGameStateUpdate(testUpdate);
        
        assertEquals(testUpdate, observer.getLastUpdate());
    }
    
    @Test
    @DisplayName("Observer can receive multiple updates")
    public void testObserverReceivesMultipleUpdates() {
        MockGameStateObserver observer = new MockGameStateObserver();
        
        GameStateUpdate update1 = new GameStateUpdate("Update 1", GamePhase.NIGHT, testPlayers);
        GameStateUpdate update2 = new GameStateUpdate("Update 2", GamePhase.DAY_DISCUSSION, testPlayers);
        GameStateUpdate update3 = new GameStateUpdate("Update 3", GamePhase.DAY_VOTING, testPlayers);
        
        observer.onGameStateUpdate(update1);
        observer.onGameStateUpdate(update2);
        observer.onGameStateUpdate(update3);
        
        assertEquals(3, observer.getUpdateCount());
        assertEquals(update3, observer.getLastUpdate());
    }
    
    @Test
    @DisplayName("Observer last update is most recent")
    public void testObserverLastUpdateIsMostRecent() {
        MockGameStateObserver observer = new MockGameStateObserver();
        
        GameStateUpdate update1 = new GameStateUpdate("First", GamePhase.NIGHT, testPlayers);
        GameStateUpdate update2 = new GameStateUpdate("Second", GamePhase.DAY_DISCUSSION, testPlayers);
        
        observer.onGameStateUpdate(update1);
        assertEquals(update1, observer.getLastUpdate());
        
        observer.onGameStateUpdate(update2);
        assertEquals(update2, observer.getLastUpdate());
    }
    
    @Test
    @DisplayName("Update count tracks callback invocations")
    public void testUpdateCountTracking() {
        MockGameStateObserver observer = new MockGameStateObserver();
        
        assertEquals(0, observer.getUpdateCount());
        
        observer.onGameStateUpdate(testUpdate);
        assertEquals(1, observer.getUpdateCount());
        
        observer.onGameStateUpdate(testUpdate);
        assertEquals(2, observer.getUpdateCount());
        
        observer.onGameStateUpdate(testUpdate);
        assertEquals(3, observer.getUpdateCount());
    }
    
    // ============== MULTIPLE OBSERVERS TESTS ==============
    
    @Test
    @DisplayName("Multiple observers can be created")
    public void testMultipleObservers() {
        MockGameStateObserver observer1 = new MockGameStateObserver();
        MockGameStateObserver observer2 = new MockGameStateObserver();
        MockGameStateObserver observer3 = new MockGameStateObserver();
        
        observer1.onGameStateUpdate(testUpdate);
        observer2.onGameStateUpdate(testUpdate);
        observer3.onGameStateUpdate(testUpdate);
        
        assertEquals(1, observer1.getUpdateCount());
        assertEquals(1, observer2.getUpdateCount());
        assertEquals(1, observer3.getUpdateCount());
    }
    
    @Test
    @DisplayName("Multiple observers are independent")
    public void testObserversAreIndependent() {
        MockGameStateObserver observer1 = new MockGameStateObserver();
        MockGameStateObserver observer2 = new MockGameStateObserver();
        
        GameStateUpdate update1 = new GameStateUpdate("Update 1", GamePhase.NIGHT, testPlayers);
        GameStateUpdate update2 = new GameStateUpdate("Update 2", GamePhase.DAY_VOTING, testPlayers);
        
        observer1.onGameStateUpdate(update1);
        observer2.onGameStateUpdate(update2);
        
        assertEquals(update1, observer1.getLastUpdate());
        assertEquals(update2, observer2.getLastUpdate());
        
        // Each has their own update count
        assertEquals(1, observer1.getUpdateCount());
        assertEquals(1, observer2.getUpdateCount());
    }
    
    @Test
    @DisplayName("One observer receiving more updates than another")
    public void testDifferentUpdateCounts() {
        MockGameStateObserver observer1 = new MockGameStateObserver();
        MockGameStateObserver observer2 = new MockGameStateObserver();
        
        observer1.onGameStateUpdate(testUpdate);
        observer1.onGameStateUpdate(testUpdate);
        observer1.onGameStateUpdate(testUpdate);
        
        observer2.onGameStateUpdate(testUpdate);
        
        assertEquals(3, observer1.getUpdateCount());
        assertEquals(1, observer2.getUpdateCount());
    }
    
    // ============== UPDATE PAYLOAD TESTS ==============
    
    @Test
    @DisplayName("Observer receives update with message")
    public void testObserverReceivesMessage() {
        MockGameStateObserver observer = new MockGameStateObserver();
        GameStateUpdate update = new GameStateUpdate("Test message", testPhase, testPlayers);
        
        observer.onGameStateUpdate(update);
        
        assertEquals("Test message", observer.getLastUpdate().getMessage());
    }
    
    @Test
    @DisplayName("Observer receives update with phase")
    public void testObserverReceivesPhase() {
        MockGameStateObserver observer = new MockGameStateObserver();
        GameStateUpdate update = new GameStateUpdate("Update", GamePhase.DAY_VOTING, testPlayers);
        
        observer.onGameStateUpdate(update);
        
        assertEquals(GamePhase.DAY_VOTING, observer.getLastUpdate().getNewPhase());
    }
    
    @Test
    @DisplayName("Observer receives update with alive players")
    public void testObserverReceivesAlivePlayers() {
        MockGameStateObserver observer = new MockGameStateObserver();
        observer.onGameStateUpdate(testUpdate);
        
        assertEquals(testPlayers, observer.getLastUpdate().getAlivePlayers());
        assertEquals(2, observer.getLastUpdate().getAlivePlayers().size());
    }
    
    @Test
    @DisplayName("Observer receives update with metadata")
    public void testObserverReceivesMetadata() {
        MockGameStateObserver observer = new MockGameStateObserver();
        
        GameStateUpdate update = new GameStateUpdate("Player died", testPhase, testPlayers);
        update.addMetadata("event_type", "death");
        update.addMetadata("player_id", "p1");
        observer.onGameStateUpdate(update);
        
        assertEquals("death", observer.getLastUpdate().getMetadata().get("event_type"));
        assertEquals("p1", observer.getLastUpdate().getMetadata().get("player_id"));
    }
    
    // ============== PHASE TRANSITION SCENARIOS ==============
    
    @Test
    @DisplayName("Observer receives night phase transitions")
    public void testNightPhaseTransition() {
        MockGameStateObserver observer = new MockGameStateObserver();
        GameStateUpdate nightUpdate = new GameStateUpdate("Night phase", GamePhase.NIGHT, testPlayers);
        
        observer.onGameStateUpdate(nightUpdate);
        
        assertEquals(GamePhase.NIGHT, observer.getLastUpdate().getNewPhase());
    }
    
    @Test
    @DisplayName("Observer receives day phase transitions")
    public void testDayPhaseTransition() {
        MockGameStateObserver observer = new MockGameStateObserver();
        GameStateUpdate dayUpdate = new GameStateUpdate("Day phase", GamePhase.DAY_VOTING, testPlayers);
        
        observer.onGameStateUpdate(dayUpdate);
        
        assertEquals(GamePhase.DAY_VOTING, observer.getLastUpdate().getNewPhase());
    }
    
    @Test
    @DisplayName("Observer tracks phase changes over time")
    public void testPhaseChanges() {
        MockGameStateObserver observer = new MockGameStateObserver();
        
        GameStateUpdate nightUpdate = new GameStateUpdate("Night", GamePhase.NIGHT, testPlayers);
        GameStateUpdate dayUpdate = new GameStateUpdate("Day", GamePhase.DAY_DISCUSSION, testPlayers);
        GameStateUpdate votingUpdate = new GameStateUpdate("Voting", GamePhase.DAY_VOTING, testPlayers);
        
        observer.onGameStateUpdate(nightUpdate);
        assertEquals(GamePhase.NIGHT, observer.getLastUpdate().getNewPhase());
        
        observer.onGameStateUpdate(dayUpdate);
        assertEquals(GamePhase.DAY_DISCUSSION, observer.getLastUpdate().getNewPhase());
        
        observer.onGameStateUpdate(votingUpdate);
        assertEquals(GamePhase.DAY_VOTING, observer.getLastUpdate().getNewPhase());
        
        assertEquals(3, observer.getUpdateCount());
    }
    
    // ============== EDGE CASES ==============
    
    @Test
    @DisplayName("Observer can reset state")
    public void testObserverReset() {
        MockGameStateObserver observer = new MockGameStateObserver();
        observer.onGameStateUpdate(testUpdate);
        
        assertEquals(1, observer.getUpdateCount());
        observer.reset();
        assertEquals(0, observer.getUpdateCount());
    }
    
    @Test
    @DisplayName("Observer can receive null update")
    public void testObserverReceivesNullUpdate() {
        MockGameStateObserver observer = new MockGameStateObserver();
        assertDoesNotThrow(() -> observer.onGameStateUpdate(null));
    }
    
    @Test
    @DisplayName("Observer can receive rapid updates")
    public void testRapidUpdates() {
        MockGameStateObserver observer = new MockGameStateObserver();
        
        for (int i = 0; i < 100; i++) {
            observer.onGameStateUpdate(testUpdate);
        }
        
        assertEquals(100, observer.getUpdateCount());
    }
}
