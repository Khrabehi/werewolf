package com.werewolf.validation;

import com.werewolf.game.*;
import com.werewolf.game.role.*;
import com.werewolf.game.action.*;
import com.werewolf.network.shared.GameCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommandOrchestrator Tests")
public class CommandOrchestratorTest {
    
    private GameSession session;
    private CommandOrchestrator orchestrator;
    private Player werewolf;
    private Player villager;
    private Player medic;
    private Player seer;
    
    @BeforeEach
    public void setUp() {
        session = new GameSession("test-session");
        orchestrator = new CommandOrchestrator(session);
        
        // Create players with different roles
        werewolf = new Player("w1", "Werewolf");
        werewolf.setRole(new WerewolfRole());
        werewolf.setAlive(true);
        
        villager = new Player("v1", "Villager");
        villager.setRole(new VillagerRole());
        villager.setAlive(true);
        
        medic = new Player("m1", "Medic");
        medic.setRole(new MedicRole());
        medic.setAlive(true);
        
        seer = new Player("s1", "Seer");
        seer.setRole(new SeerRole());
        seer.setAlive(true);
        
        session.addPlayer(werewolf);
        session.addPlayer(villager);
        session.addPlayer(medic);
        session.addPlayer(seer);
    }
    
    // ============== KILL ACTION TESTS ==============
    
    @Test
    @DisplayName("Werewolf can kill at night")
    public void testWerewolfCanKillAtNight() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand killCmd = new GameCommand("KILL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("w1", killCmd);
        
        assertTrue(result.isSuccess(), "Werewolf should be able to kill at night");
        assertFalse(villager.isAlive(), "Villager should be dead after kill");
    }
    
    @Test
    @DisplayName("Villager cannot kill")
    public void testVillagerCannotKill() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand killCmd = new GameCommand("KILL", "w1");
        CommandExecutionResult result = orchestrator.executeCommand("v1", killCmd);
        
        assertFalse(result.isSuccess(), "Villager should not be able to kill");
        assertTrue(result.getErrorMessage().contains("not allowed"));
    }
    
    @Test
    @DisplayName("Medic cannot kill")
    public void testMedicCannotKill() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand killCmd = new GameCommand("KILL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("m1", killCmd);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("not allowed"));
    }
    
    @Test
    @DisplayName("Kill only allowed at night")
    public void testKillOnlyAtNight() {
        session.updatePhase(GamePhase.DAY_VOTING);
        
        GameCommand killCmd = new GameCommand("KILL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("w1", killCmd);
        
        assertFalse(result.isSuccess(), "Kill should only work at night");
        assertTrue(result.getErrorMessage().contains("night"));
    }
    
    @Test
    @DisplayName("Cannot target yourself")
    public void testCannotTargetYourself() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand killCmd = new GameCommand("KILL", "w1");
        CommandExecutionResult result = orchestrator.executeCommand("w1", killCmd);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Cannot target yourself"));
    }
    
    @Test
    @DisplayName("Cannot kill dead player")
    public void testCannotKillDeadPlayer() {
        villager.setAlive(false);
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand killCmd = new GameCommand("KILL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("w1", killCmd);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("dead"));
    }
    
    @Test
    @DisplayName("Dead player cannot act")
    public void testDeadPlayerCannotAct() {
        werewolf.setAlive(false);
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand killCmd = new GameCommand("KILL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("w1", killCmd);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Dead"));
    }
    
    // ============== VOTE ACTION TESTS ==============
    
    @Test
    @DisplayName("Villager can vote during day voting")
    public void testVillagerCanVote() {
        session.updatePhase(GamePhase.DAY_VOTING);
        
        GameCommand voteCmd = new GameCommand("VOTE", "w1");
        CommandExecutionResult result = orchestrator.executeCommand("v1", voteCmd);
        
        assertTrue(result.isSuccess(), "Villager should be able to vote");
    }
    
    @Test
    @DisplayName("All roles can vote during day voting")
    public void testAllRolesCanVote() {
        session.updatePhase(GamePhase.DAY_VOTING);
        
        // Test each role can vote (each player votes for a different target)
        String[] voters = {"w1", "v1", "m1", "s1"};
        String[] targets = {"v1", "w1", "s1", "m1"};
        
        for (int i = 0; i < voters.length; i++) {
            GameCommand voteCmd = new GameCommand("VOTE", targets[i]);
            CommandExecutionResult result = orchestrator.executeCommand(voters[i], voteCmd);
            assertTrue(result.isSuccess(), voters[i] + " should be able to vote");
        }
    }
    
    @Test
    @DisplayName("Vote only allowed during day voting phase")
    public void testVoteOnlyDuringDayVoting() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand voteCmd = new GameCommand("VOTE", "w1");
        CommandExecutionResult result = orchestrator.executeCommand("v1", voteCmd);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("day voting"));
    }
    
    // ============== PROTECT ACTION TESTS ==============
    
    @Test
    @DisplayName("Medic can protect at night")
    public void testMedicCanProtect() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand protectCmd = new GameCommand("HEAL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("m1", protectCmd);
        
        assertTrue(result.isSuccess(), "Medic should be able to protect");
        assertTrue(villager.isProtected(), "Villager should be protected");
    }
    
    @Test
    @DisplayName("Villager cannot protect")
    public void testVillagerCannotProtect() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand protectCmd = new GameCommand("HEAL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("v1", protectCmd);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("not allowed"));
    }
    
    // ============== INVESTIGATE ACTION TESTS ==============
    
    @Test
    @DisplayName("Seer can investigate at night")
    public void testSeerCanInvestigate() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand investigateCmd = new GameCommand("PEEK", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("s1", investigateCmd);
        
        assertTrue(result.isSuccess(), "Seer should be able to investigate");
    }
    
    @Test
    @DisplayName("Villager cannot investigate")
    public void testVillagerCannotInvestigate() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand investigateCmd = new GameCommand("PEEK", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("v1", investigateCmd);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("not allowed"));
    }
    
    // ============== PROTECTION MECHANIC TESTS ==============
    
    @Test
    @DisplayName("Protected player survives kill")
    public void testProtectionMechanic() {
        session.updatePhase(GamePhase.NIGHT);
        
        // Step 1: Medic protects the villager
        GameCommand protectCmd = new GameCommand("HEAL", "v1");
        CommandExecutionResult protectResult = orchestrator.executeCommand("m1", protectCmd);
        assertTrue(protectResult.isSuccess());
        assertTrue(villager.isProtected());
        
        // Step 2: Werewolf tries to kill the protected villager
        GameCommand killCmd = new GameCommand("KILL", "v1");
        CommandExecutionResult killResult = orchestrator.executeCommand("w1", killCmd);
        assertTrue(killResult.isSuccess());
        
        // Step 3: Verify villager is still alive
        assertTrue(villager.isAlive(), "Protected villager should survive");
    }
    
    @Test
    @DisplayName("Unprotected player dies from kill")
    public void testUnprotectedPlayerDies() {
        session.updatePhase(GamePhase.NIGHT);
        assertFalse(villager.isProtected(), "Villager should not be protected initially");
        
        GameCommand killCmd = new GameCommand("KILL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("w1", killCmd);
        
        assertTrue(result.isSuccess());
        assertFalse(villager.isAlive(), "Unprotected villager should die");
    }
    
    // ============== INVALID PLAYER TESTS ==============
    
    @Test
    @DisplayName("Invalid player ID returns error")
    public void testInvalidPlayerId() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand killCmd = new GameCommand("KILL", "invalid_id");
        CommandExecutionResult result = orchestrator.executeCommand("w1", killCmd);
        
        assertFalse(result.isSuccess());
    }
    
    @Test
    @DisplayName("Non-existent player cannot perform action")
    public void testNonExistentPlayerCannotAct() {
        session.updatePhase(GamePhase.NIGHT);
        
        GameCommand killCmd = new GameCommand("KILL", "v1");
        CommandExecutionResult result = orchestrator.executeCommand("non_existent", killCmd);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("not found"));
    }
}
