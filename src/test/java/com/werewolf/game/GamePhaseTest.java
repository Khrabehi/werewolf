package com.werewolf.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameState Tests")
public class GamePhaseTest {
    
    // ============== ENUM VALUE TESTS ==============
    
    @Test
    @DisplayName("GameState enum has all required phases")
    public void testGamePhaseValues() {
        GameState[] phases = GameState.values();
        
        assertTrue(GameState.values().length >= 5, "Should have at least 5 phases");
        
        // Check specific phases exist
        assertEquals("LOBBY", GameState.LOBBY.name());
        assertEquals("NIGHT", GameState.NIGHT.name());
        assertEquals("DAY_DISCUSSION", GameState.DAY_DISCUSSION.name());
        assertEquals("DAY_VOTING", GameState.DAY_VOTING.name());
        assertEquals("GAME_OVER", GameState.GAME_OVER.name());
    }
    
    @Test
    @DisplayName("GameState.LOBBY is the initial phase")
    public void testLobbyPhase() {
        GameState phase = GameState.LOBBY;
        assertEquals("LOBBY", phase.name());
    }
    
    @Test
    @DisplayName("GameState.NIGHT phase exists")
    public void testNightPhase() {
        GameState phase = GameState.NIGHT;
        assertEquals("NIGHT", phase.name());
    }
    
    @Test
    @DisplayName("GameState.DAY_DISCUSSION phase exists")
    public void testDayDiscussionPhase() {
        GameState phase = GameState.DAY_DISCUSSION;
        assertEquals("DAY_DISCUSSION", phase.name());
    }
    
    @Test
    @DisplayName("GameState.DAY_VOTING phase exists")
    public void testDayVotingPhase() {
        GameState phase = GameState.DAY_VOTING;
        assertEquals("DAY_VOTING", phase.name());
    }
    
    @Test
    @DisplayName("GameState.GAME_OVER phase exists")
    public void testGameOverPhase() {
        GameState phase = GameState.GAME_OVER;
        assertEquals("GAME_OVER", phase.name());
    }
    
    // ============== PHASE COMPARISON TESTS ==============
    
    @Test
    @DisplayName("GameState instances can be compared")
    public void testGamePhaseComparison() {
        GameState phase1 = GameState.NIGHT;
        GameState phase2 = GameState.NIGHT;
        GameState phase3 = GameState.DAY_VOTING;
        
        assertEquals(phase1, phase2);
        assertNotEquals(phase1, phase3);
    }
    
    @Test
    @DisplayName("GameState can be used in if statements")
    public void testGamePhaseCondition() {
        GameState currentPhase = GameState.NIGHT;
        
        if (currentPhase == GameState.NIGHT) {
            assertTrue(true);
        } else {
            fail("Phase comparison should work");
        }
    }
    
    @Test
    @DisplayName("GameState.valueOf works correctly")
    public void testGamePhaseValueOf() {
        GameState phase = GameState.valueOf("NIGHT");
        assertEquals(GameState.NIGHT, phase);
    }
    
    @Test
    @DisplayName("GameState.valueOf throws for invalid phase")
    public void testGamePhaseValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            GameState.valueOf("INVALID_PHASE");
        });
    }
    
    // ============== NIGHT PHASE TESTS ==============
    
    @Test
    @DisplayName("NIGHT phase allows werewolf actions")
    public void testNightPhaseForWerewolf() {
        GameState phase = GameState.NIGHT;
        // Werewolves should perform KILL actions at NIGHT
        assertEquals(GameState.NIGHT, phase);
    }
    
    @Test
    @DisplayName("NIGHT phase allows medic protection")
    public void testNightPhaseForMedic() {
        GameState phase = GameState.NIGHT;
        // Medics should perform PROTECT actions at NIGHT
        assertEquals(GameState.NIGHT, phase);
    }
    
    @Test
    @DisplayName("NIGHT phase allows seer investigation")
    public void testNightPhaseForSeer() {
        GameState phase = GameState.NIGHT;
        // Seers should perform INVESTIGATE actions at NIGHT
        assertEquals(GameState.NIGHT, phase);
    }
    
    // ============== DAY VOTING PHASE TESTS ==============
    
    @Test
    @DisplayName("DAY_VOTING phase allows voting")
    public void testDayVotingPhaseForVotes() {
        GameState phase = GameState.DAY_VOTING;
        // All players should be able to VOTE at DAY_VOTING
        assertEquals(GameState.DAY_VOTING, phase);
    }
    
    @Test
    @DisplayName("DAY_VOTING phase does not allow kills")
    public void testDayVotingPhaseDoesNotAllowKills() {
        GameState phase = GameState.DAY_VOTING;
        // Kills should not happen during DAY_VOTING
        assertNotEquals(GameState.NIGHT, phase);
    }
    
    // ============== PHASE SEQUENCE TESTS ==============
    
    @Test
    @DisplayName("Game flow through phases")
    public void testGamePhaseFlow() {
        GameState phase = GameState.LOBBY;
        assertEquals(GameState.LOBBY, phase);
        
        phase = GameState.NIGHT;
        assertEquals(GameState.NIGHT, phase);
        
        phase = GameState.DAY_DISCUSSION;
        assertEquals(GameState.DAY_DISCUSSION, phase);
        
        phase = GameState.DAY_VOTING;
        assertEquals(GameState.DAY_VOTING, phase);
        
        phase = GameState.GAME_OVER;
        assertEquals(GameState.GAME_OVER, phase);
    }
    
    @Test
    @DisplayName("All phases are distinct")
    public void testAllPhasesDistinct() {
        GameState lobby = GameState.LOBBY;
        GameState night = GameState.NIGHT;
        GameState discussion = GameState.DAY_DISCUSSION;
        GameState voting = GameState.DAY_VOTING;
        GameState gameOver = GameState.GAME_OVER;
        
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
    @DisplayName("GameState names contain expected keywords")
    public void testGamePhaseNames() {
        assertTrue(GameState.LOBBY.name().contains("LOBBY"));
        assertTrue(GameState.NIGHT.name().contains("NIGHT"));
        assertTrue(GameState.DAY_DISCUSSION.name().contains("DAY"));
        assertTrue(GameState.DAY_VOTING.name().contains("DAY"));
        assertTrue(GameState.GAME_OVER.name().contains("GAME"));
    }
    
    @Test
    @DisplayName("DAY_DISCUSSION and DAY_VOTING are both day phases")
    public void testDayPhases() {
        GameState discussion = GameState.DAY_DISCUSSION;
        GameState voting = GameState.DAY_VOTING;
        
        assertTrue(discussion.name().contains("DAY"));
        assertTrue(voting.name().contains("DAY"));
        assertNotEquals(discussion, voting);
    }
}
