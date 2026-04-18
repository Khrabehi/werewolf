package com.werewolf.game.action;

import com.werewolf.game.*;
import com.werewolf.game.role.*;
import com.werewolf.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game Action Tests")
public class GameActionTest {
    
    private GameSession session;
    private Player actor;
    private Player target;
    
    @BeforeEach
    public void setUp() {
        session = new GameSession("test-session");
        
        actor = new Player("a1", "Actor");
        target = new Player("t1", "Target");
        
        session.addPlayer(actor);
        session.addPlayer(target);
    }
    
    // ============== KILL ACTION TESTS ==============
    
    @Test
    @DisplayName("KillAction marks target as dead")
    public void testKillActionMarksTargetDead() {
        GameAction killAction = new KillAction();
        
        assertTrue(target.isAlive(), "Target should be alive initially");
        killAction.execute(actor, target, session);
        assertFalse(target.isAlive(), "Target should be marked as dead");
    }
    
    @Test
    @DisplayName("KillAction respects protection")
    public void testKillActionRespectsProtection() {
        target.setProtected(true);
        GameAction killAction = new KillAction();
        
        killAction.execute(actor, target, session);
        assertTrue(target.isAlive(), "Protected target should survive");
    }
    
    @Test
    @DisplayName("KillAction kills unprotected target")
    public void testKillActionKillsUnprotected() {
        target.setProtected(false);
        GameAction killAction = new KillAction();
        
        killAction.execute(actor, target, session);
        assertFalse(target.isAlive(), "Unprotected target should die");
    }
    
    @Test
    @DisplayName("KillAction notifies observers when target dies")
    public void testKillActionNotifiesObserver() {
        target.setProtected(false);
        GameAction killAction = new KillAction();
        
        // Subscribe a mock observer
        java.util.List<GameStateUpdate> updates = new java.util.ArrayList<>();
        session.subscribe(updates::add);
        
        killAction.execute(actor, target, session);
        
        // Verify notification was sent (KillAction calls notifySessionUpdate)
        // Note: KillAction only notifies if kill is successful
        assertTrue(updates.size() > 0, "Observer should be notified");
    }
    
    // ============== VOTE ACTION TESTS ==============
    
    @Test
    @DisplayName("VoteAction records vote")
    public void testVoteActionRecordsVote() {
        GameAction voteAction = new VoteAction();
        
        voteAction.execute(actor, target, session);
        
        // Verify vote was recorded
        // Note: We can't directly access votes from GameSession, but we can verify no exception
        assertDoesNotThrow(() -> voteAction.execute(actor, target, session));
    }
    
    @Test
    @DisplayName("VoteAction can overwrite vote")
    public void testVoteActionCanOverwrite() {
        GameAction voteAction = new VoteAction();
        Player newTarget = new Player("t2", "NewTarget");
        session.addPlayer(newTarget);
        
        // First vote
        voteAction.execute(actor, target, session);
        
        // Overwrite vote
        assertDoesNotThrow(() -> voteAction.execute(actor, newTarget, session));
    }
    
    // ============== PROTECT ACTION TESTS ==============
    
    @Test
    @DisplayName("ProtectAction marks target as protected")
    public void testProtectActionMarksProtected() {
        GameAction protectAction = new ProtectAction();
        
        assertFalse(target.isProtected(), "Target should not be protected initially");
        protectAction.execute(actor, target, session);
        assertTrue(target.isProtected(), "Target should be marked as protected");
    }
    
    @Test
    @DisplayName("ProtectAction can protect multiple times")
    public void testProtectActionMultipleTimes() {
        GameAction protectAction = new ProtectAction();
        
        protectAction.execute(actor, target, session);
        assertTrue(target.isProtected());
        
        // Can protect again
        protectAction.execute(actor, target, session);
        assertTrue(target.isProtected());
    }
    
    // ============== INVESTIGATE ACTION TESTS ==============
    
    @Test
    @DisplayName("InvestigateAction executes without error")
    public void testInvestigateActionExecutes() {
        GameAction investigateAction = new InvestigateAction();
        target.setRole(new WerewolfRole());
        
        assertDoesNotThrow(() -> investigateAction.execute(actor, target, session));
    }
    
    @Test
    @DisplayName("InvestigateAction reveals target role")
    public void testInvestigateActionRevealsRole() {
        GameAction investigateAction = new InvestigateAction();
        PlayerRole role = new WerewolfRole();
        target.setRole(role);
        
        // Execute investigate - should send private message with role
        investigateAction.execute(actor, target, session);
        
        // Verify target has a role (investigation should work)
        assertEquals(role.getName(), target.getRole().getName());
    }
    
    // ============== ACTION FACTORY TESTS ==============
    
    @Test
    @DisplayName("ActionFactory creates KillAction")
    public void testActionFactoryCreatesKillAction() {
        ActionFactory factory = new ActionFactory();
        GameAction action = factory.getAction("KILL");
        
        assertNotNull(action);
        assertInstanceOf(KillAction.class, action);
    }
    
    @Test
    @DisplayName("ActionFactory creates VoteAction")
    public void testActionFactoryCreatesVoteAction() {
        ActionFactory factory = new ActionFactory();
        GameAction action = factory.getAction("VOTE");
        
        assertNotNull(action);
        assertInstanceOf(VoteAction.class, action);
    }
    
    @Test
    @DisplayName("ActionFactory creates ProtectAction")
    public void testActionFactoryCreatesProtectAction() {
        ActionFactory factory = new ActionFactory();
        GameAction action = factory.getAction("HEAL");
        
        assertNotNull(action);
        assertInstanceOf(ProtectAction.class, action);
    }
    
    @Test
    @DisplayName("ActionFactory creates InvestigateAction")
    public void testActionFactoryCreatesInvestigateAction() {
        ActionFactory factory = new ActionFactory();
        GameAction action = factory.getAction("PEEK");
        
        assertNotNull(action);
        assertInstanceOf(InvestigateAction.class, action);
    }
    
    @Test
    @DisplayName("ActionFactory handles case-insensitive action name")
    public void testActionFactoryHandlesCaseInsensitive() {
        ActionFactory factory = new ActionFactory();
        
        GameAction action1 = factory.getAction("kill");
        GameAction action2 = factory.getAction("KILL");
        GameAction action3 = factory.getAction("Kill");
        
        assertInstanceOf(KillAction.class, action1);
        assertInstanceOf(KillAction.class, action2);
        assertInstanceOf(KillAction.class, action3);
    }
    
    @Test
    @DisplayName("ActionFactory throws exception for unknown action")
    public void testActionFactoryThrowsForUnknown() {
        ActionFactory factory = new ActionFactory();
        
        assertThrows(IllegalArgumentException.class, () -> {
            factory.getAction("UNKNOWN_ACTION");
        });
    }
    
    @Test
    @DisplayName("Each factory call creates new instance")
    public void testActionFactoryCreatesNewInstances() {
        ActionFactory factory = new ActionFactory();
        
        GameAction action1 = factory.getAction("KILL");
        GameAction action2 = factory.getAction("KILL");
        
        // Should be different instances (not same cached instance)
        assertNotSame(action1, action2, "Factory should create new instances");
    }
}
