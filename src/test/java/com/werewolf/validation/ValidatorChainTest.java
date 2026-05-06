package com.werewolf.validation;

import com.werewolf.game.*;
import com.werewolf.game.role.*;
import com.werewolf.network.shared.GameCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validator Chain Tests")
public class ValidatorChainTest {
    
    private GameSession session;
    private Player werewolf;
    private Player villager;
    private GameCommand killCommand;
    
    @BeforeEach
    public void setUp() {
        session = new GameSession("test-session");
        
        werewolf = new Player("w1", "Werewolf");
        werewolf.setRole(new WerewolfRole());
        werewolf.setAlive(true);
        
        villager = new Player("v1", "Villager");
        villager.setRole(new VillagerRole());
        villager.setAlive(true);
        
        session.addPlayer(werewolf);
        session.addPlayer(villager);
        
        killCommand = new GameCommand("KILL", "v1");
        session.updatePhase(GamePhase.NIGHT);
    }
    
    // ============== PLAYER EXISTENCE VALIDATOR ==============
    
    @Test
    @DisplayName("PlayerExistenceValidator accepts valid player")
    public void testPlayerExistenceValidatorAcceptsValid() {
        PlayerExistenceValidator validator = new PlayerExistenceValidator();
        validator.setNext(new PlayerAliveValidator());
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertTrue(result.isValid(), "Should accept valid player");
    }
    
    @Test
    @DisplayName("PlayerExistenceValidator rejects null player")
    public void testPlayerExistenceValidatorRejectsNull() {
        PlayerExistenceValidator validator = new PlayerExistenceValidator();
        
        ValidationResult result = validator.validate(killCommand, null, session);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("not found"));
    }
    
    // ============== PLAYER ALIVE VALIDATOR ==============
    
    @Test
    @DisplayName("PlayerAliveValidator accepts alive player")
    public void testPlayerAliveValidatorAcceptsAlive() {
        PlayerAliveValidator validator = new PlayerAliveValidator();
        assertTrue(werewolf.isAlive());
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertTrue(result.isValid(), "Should accept alive player");
    }
    
    @Test
    @DisplayName("PlayerAliveValidator rejects dead player")
    public void testPlayerAliveValidatorRejectsDead() {
        werewolf.setAlive(false);
        PlayerAliveValidator validator = new PlayerAliveValidator();
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Dead"));
    }
    
    // ============== ROLE PERMISSION VALIDATOR ==============
    
    @Test
    @DisplayName("RolePermissionValidator accepts allowed action")
    public void testRolePermissionValidatorAcceptsAllowed() {
        RolePermissionValidator validator = new RolePermissionValidator();
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertTrue(result.isValid(), "Werewolf should be allowed to kill");
    }
    
    @Test
    @DisplayName("RolePermissionValidator rejects disallowed action")
    public void testRolePermissionValidatorRejectsDisallowed() {
        RolePermissionValidator validator = new RolePermissionValidator();
        
        ValidationResult result = validator.validate(killCommand, villager, session);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("not allowed"));
    }
    
    @Test
    @DisplayName("RolePermissionValidator rejects null role")
    public void testRolePermissionValidatorRejectsNullRole() {
        RolePermissionValidator validator = new RolePermissionValidator();
        werewolf.setRole(null);
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertFalse(result.isValid());
    }
    
    // ============== GAME STATE VALIDATOR ==============
    
    @Test
    @DisplayName("GameStateValidator accepts kill at night")
    public void testGameStateValidatorAcceptsKillAtNight() {
        session.updatePhase(GamePhase.NIGHT);
        GameStateValidator validator = new GameStateValidator();
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertTrue(result.isValid(), "Kill should be allowed at night");
    }
    
    @Test
    @DisplayName("GameStateValidator rejects kill during day")
    public void testGameStateValidatorRejectsKillDuringDay() {
        session.updatePhase(GamePhase.DAY_DISCUSSION);
        GameStateValidator validator = new GameStateValidator();
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("night"));
    }
    
    @Test
    @DisplayName("GameStateValidator rejects vote during night")
    public void testGameStateValidatorRejectsVoteDuringNight() {
        session.updatePhase(GamePhase.NIGHT);
        GameCommand voteCommand = new GameCommand("VOTE", "v1");
        GameStateValidator validator = new GameStateValidator();
        
        ValidationResult result = validator.validate(voteCommand, villager, session);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("day voting"));
    }
    
    @Test
    @DisplayName("GameStateValidator accepts vote during day voting")
    public void testGameStateValidatorAcceptsVoteDuringDayVoting() {
        session.updatePhase(GamePhase.DAY_VOTING);
        GameCommand voteCommand = new GameCommand("VOTE", "w1");
        GameStateValidator validator = new GameStateValidator();
        
        ValidationResult result = validator.validate(voteCommand, villager, session);
        assertTrue(result.isValid(), "Vote should be allowed during day voting");
    }
    
    // ============== TARGET VALIDATION VALIDATOR ==============
    
    @Test
    @DisplayName("TargetValidationValidator accepts valid target")
    public void testTargetValidationValidatorAcceptsValid() {
        TargetValidationValidator validator = new TargetValidationValidator();
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertTrue(result.isValid(), "Valid target should be accepted");
    }
    
    @Test
    @DisplayName("TargetValidationValidator rejects null target")
    public void testTargetValidationValidatorRejectsNull() {
        GameCommand noTargetCmd = new GameCommand("KILL", null);
        TargetValidationValidator validator = new TargetValidationValidator();
        
        ValidationResult result = validator.validate(noTargetCmd, werewolf, session);
        assertFalse(result.isValid());
    }
    
    @Test
    @DisplayName("TargetValidationValidator rejects dead target")
    public void testTargetValidationValidatorRejectsDead() {
        villager.setAlive(false);
        TargetValidationValidator validator = new TargetValidationValidator();
        
        ValidationResult result = validator.validate(killCommand, werewolf, session);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("dead"));
    }
    
    @Test
    @DisplayName("TargetValidationValidator rejects self-target")
    public void testTargetValidationValidatorRejectsSelfTarget() {
        GameCommand selfKillCmd = new GameCommand("KILL", "w1");
        TargetValidationValidator validator = new TargetValidationValidator();
        
        ValidationResult result = validator.validate(selfKillCmd, werewolf, session);
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Cannot target yourself"));
    }
    
    // ============== Chain Flow Tests ==============
    
    @Test
    @DisplayName("Full validator chain returns valid for valid command")
    public void testFullChainReturnsValidForValidCommand() {
        Validator chain = new PlayerExistenceValidator();
        chain.setNext(new PlayerAliveValidator())
             .setNext(new RolePermissionValidator())
             .setNext(new GameStateValidator())
             .setNext(new TargetValidationValidator());
        
        session.updatePhase(GamePhase.NIGHT);
        ValidationResult result = chain.validate(killCommand, werewolf, session);
        
        assertTrue(result.isValid(), "Valid command should pass full chain");
    }
    
    @Test
    @DisplayName("Full validator chain stops at first failure")
    public void testFullChainStopsAtFirstFailure() {
        Validator chain = new PlayerExistenceValidator();
        chain.setNext(new PlayerAliveValidator())
             .setNext(new RolePermissionValidator())
             .setNext(new GameStateValidator())
             .setNext(new TargetValidationValidator());
        
        werewolf.setAlive(false);  // This should be caught by PlayerAliveValidator
        session.updatePhase(GamePhase.NIGHT);
        
        ValidationResult result = chain.validate(killCommand, werewolf, session);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("Dead"));
    }
}
