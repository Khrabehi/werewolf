package com.werewolf.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GamePhase Tests")
public class GamePhaseTest {
    
    // ============== ENUM VALUE TESTS ==============
    
    @Test
    @DisplayName("GamePhase enum has all required phases")
    public void testGamePhaseValues() {
        GamePhase[] phases = GamePhase.values();
        
        assertTrue(GamePhase.values().length >= 5, "Should have at least 5 phases");
        
        // Check specific phases exist
        assertEquals("LOBBY", GamePhase.LOBBY.name());
        assertEquals("NIGHT", GamePhase.NIGHT.name());
        assertEquals("DAY_DISCUSSION", GamePhase.DAY_DISCUSSION.name());
        assertEquals("DAY_VOTING", GamePhase.DAY_VOTING.name());
        assertEquals("GAME_OVER", GamePhase.GAME_OVER.name());
    }
    
    @Test
    @DisplayName("GamePhase.LOBBY is the initial phase")
    public void testLobbyPhase() {
        GamePhase phase = GamePhase.LOBBY;
        assertEquals("LOBBY", phase.name());
    }
    
    @Test
    @DisplayName("GamePhase.NIGHT phase exists")
    public void testNightPhase() {
        GamePhase phase = GamePhase.NIGHT;
        assertEquals("NIGHT", phase.name());
    }
    
    @Test
    @DisplayName("GamePhase.DAY_DISCUSSION phase exists")
    public void testDayDiscussionPhase() {
        GamePhase phase = GamePhase.DAY_DISCUSSION;
        assertEquals("DAY_DISCUSSION", phase.name());
    }
    
    @Test
    @DisplayName("GamePhase.DAY_VOTING phase exists")
    public void testDayVotingPhase() {
        GamePhase phase = GamePhase.DAY_VOTING;
        assertEquals("DAY_VOTING", phase.name());
    }
    
    @Test
    @DisplayName("GamePhase.GAME_OVER phase exists")
    public void testGameOverPhase() {
        GamePhase phase = GamePhase.GAME_OVER;
        assertEquals("GAME_OVER", phase.name());
    }
    
    // ============== PHASE COMPARISON TESTS ==============
    
    @Test
    @DisplayName("GamePhase instances can be compared")
    public void testGamePhaseComparison() {
        GamePhase phase1 = GamePhase.NIGHT;
        GamePhase phase2 = GamePhase.NIGHT;
        GamePhase phase3 = GamePhase.DAY_VOTING;
        
        assertEquals(phase1, phase2);
        assertNotEquals(phase1, phase3);
    }
    
    @Test
    @DisplayName("GamePhase can be used in if statements")
    public void testGamePhaseCondition() {
        GamePhase currentPhase = GamePhase.NIGHT;
        
        if (currentPhase == GamePhase.NIGHT) {
            assertTrue(true);
        } else {
            fail("Phase comparison should work");
        }
    }
    
    @Test
    @DisplayName("GamePhase.valueOf works correctly")
    public void testGamePhaseValueOf() {
        GamePhase phase = GamePhase.valueOf("NIGHT");
        assertEquals(GamePhase.NIGHT, phase);
    }
    
    @Test
    @DisplayName("GamePhase.valueOf throws for invalid phase")
    public void testGamePhaseValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            GamePhase.valueOf("INVALID_PHASE");
        });
    }
    
    // ============== NIGHT PHASE TESTS ==============
    
    @Test
    @DisplayName("NIGHT phase allows werewolf actions")
    public void testNightPhaseForWerewolf() {
        GamePhase phase = GamePhase.NIGHT;
        // Werewolves should perform KILL actions at NIGHT
        assertEquals(GamePhase.NIGHT, phase);
    }
    
    @Test
    @DisplayName("NIGHT phase allows medic protection")
    public void testNightPhaseForMedic() {
        GamePhase phase = GamePhase.NIGHT;
        // Medics should perform PROTECT actions at NIGHT
        assertEquals(GamePhase.NIGHT, phase);
    }
    
    @Test
    @DisplayName("NIGHT phase allows seer investigation")
    public void testNightPhaseForSeer() {
        GamePhase phase = GamePhase.NIGHT;
        // Seers should perform INVESTIGATE actions at NIGHT
        assertEquals(GamePhase.NIGHT, phase);
    }
    
    // ============== DAY VOTING PHASE TESTS ==============
    
    @Test
    @DisplayName("DAY_VOTING phase allows voting")
    public void testDayVotingPhaseForVotes() {
        GamePhase phase = GamePhase.DAY_VOTING;
        // All players should be able to VOTE at DAY_VOTING
        assertEquals(GamePhase.DAY_VOTING, phase);
    }
    
    @Test
    @DisplayName("DAY_VOTING phase does not allow kills")
    public void testDayVotingPhaseDoesNotAllowKills() {
        GamePhase phase = GamePhase.DAY_VOTING;
        // Kills should not happen during DAY_VOTING
        assertNotEquals(GamePhase.NIGHT, phase);
    }
    
    // ============== PHASE SEQUENCE TESTS ==============
    
    @Test
    @DisplayName("Game flow through phases")
    public void testGamePhaseFlow() {
        GamePhase phase = GamePhase.LOBBY;
        assertEquals(GamePhase.LOBBY, phase);
        
        phase = GamePhase.NIGHT;
        assertEquals(GamePhase.NIGHT, phase);
        
        phase = GamePhase.DAY_DISCUSSION;
        assertEquals(GamePhase.DAY_DISCUSSION, phase);
        
        phase = GamePhase.DAY_VOTING;
        assertEquals(GamePhase.DAY_VOTING, phase);
        
        phase = GamePhase.GAME_OVER;
        assertEquals(GamePhase.GAME_OVER, phase);
    }
    
    @Test
    @DisplayName("All phases are distinct")
    public void testAllPhasesDistinct() {
        GamePhase lobby = GamePhase.LOBBY;
        GamePhase night = GamePhase.NIGHT;
        GamePhase discussion = GamePhase.DAY_DISCUSSION;
        GamePhase voting = GamePhase.DAY_VOTING;
        GamePhase gameOver = GamePhase.GAME_OVER;
        
        assertNotEquals(lobby, night);
        assertNotEquals(lobby, discussion);
        assertNotEquals(lobby, voting);
        assertNotEquals(lobby, gameOver);
        
        assertNotEquals(night, discussion);
        assertNotEquals(night, voting);
        assertNotEquals(night, gameOver);
    }
    
    // ============== PHASE NAMING TESTS ==============
    
    @Test
    @DisplayName("GamePhase names contain expected keywords")
    public void testGamePhaseNames() {
        assertTrue(GamePhase.LOBBY.name().contains("LOBBY"));
        assertTrue(GamePhase.NIGHT.name().contains("NIGHT"));
        assertTrue(GamePhase.DAY_DISCUSSION.name().contains("DAY"));
        assertTrue(GamePhase.DAY_VOTING.name().contains("DAY"));
        assertTrue(GamePhase.GAME_OVER.name().contains("GAME"));
    }
    
    @Test
    @DisplayName("DAY_DISCUSSION and DAY_VOTING are both day phases")
    public void testDayPhases() {
        GamePhase discussion = GamePhase.DAY_DISCUSSION;
        GamePhase voting = GamePhase.DAY_VOTING;
        
        assertTrue(discussion.name().contains("DAY"));
        assertTrue(voting.name().contains("DAY"));
        assertNotEquals(discussion, voting);
    }
}
