package com.werewolf.game;

import com.werewolf.game.role.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Player Tests")
public class PlayerTest {
    
    private Player player;
    private PlayerRole villagerRole;
    private PlayerRole werewolfRole;
    private PlayerRole medicRole;
    
    @BeforeEach
    public void setUp() {
        villagerRole = new VillagerRole();
        werewolfRole = new WerewolfRole();
        medicRole = new MedicRole();
    }
    
    // ============== CONSTRUCTOR & INITIALIZATION TESTS ==============
    
    @Test
    @DisplayName("Player initializes with correct properties")
    public void testPlayerInitialization() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        
        assertEquals("player1", player.getId());
        assertEquals("Alice", player.getUsername());
        assertEquals(villagerRole, player.getRole());
        assertTrue(player.isAlive());
        assertFalse(player.isProtected());
    }
    
    @Test
    @DisplayName("Player can be created with different roles")
    public void testPlayerWithDifferentRoles() {
        Player villager = new Player("p1", "Alice");
        villager.setRole(villagerRole);
        
        Player werewolf = new Player("p2", "Bob");
        werewolf.setRole(werewolfRole);
        
        Player medic = new Player("p3", "Carol");
        medic.setRole(medicRole);
        
        assertEquals(villagerRole, villager.getRole());
        assertEquals(werewolfRole, werewolf.getRole());
        assertEquals(medicRole, medic.getRole());
    }
    
    // ============== ID & USERNAME TESTS ==============
    
    @Test
    @DisplayName("Player ID is immutable")
    public void testPlayerIdImmutable() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        String originalId = player.getId();
        
        assertEquals("player1", originalId);
        // Verify it stays the same on multiple calls
        assertEquals("player1", player.getId());
    }
    
    @Test
    @DisplayName("Player username can be retrieved")
    public void testPlayerUsername() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        assertEquals("Alice", player.getUsername());
    }
    
    // ============== ROLE TESTS ==============
    
    @Test
    @DisplayName("Player role can be retrieved")
    public void testPlayerRole() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        assertEquals(villagerRole, player.getRole());
    }
    
    @Test
    @DisplayName("Player can change role")
    public void testPlayerRoleChange() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        assertEquals(villagerRole, player.getRole());
        
        player.setRole(werewolfRole);
        assertEquals(werewolfRole, player.getRole());
    }
    
    @Test
    @DisplayName("Player role can be set to null")
    public void testPlayerRoleNull() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        player.setRole(null);
        assertNull(player.getRole());
    }
    
    // ============== ALIVE STATE TESTS ==============
    
    @Test
    @DisplayName("Player starts alive")
    public void testPlayerStartsAlive() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        assertTrue(player.isAlive());
    }
    
    @Test
    @DisplayName("Player can be marked dead")
    public void testPlayerCanBeMarkedDead() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        player.setAlive(false);
        assertFalse(player.isAlive());
    }
    
    @Test
    @DisplayName("Player can be revived")
    public void testPlayerCanBeRevived() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        player.setAlive(false);
        assertFalse(player.isAlive());
        
        player.setAlive(true);
        assertTrue(player.isAlive());
    }
    
    @Test
    @DisplayName("Player dead state is independent")
    public void testPlayerDeadStateIndependent() {
        Player player1 = new Player("p1", "Alice");
        player1.setRole(villagerRole);
        Player player2 = new Player("p2", "Bob");
        player2.setRole(werewolfRole);
        
        player1.setAlive(false);
        assertTrue(player2.isAlive());
        assertFalse(player1.isAlive());
    }
    
    // ============== PROTECTION STATE TESTS ==============
    
    @Test
    @DisplayName("Player starts unprotected")
    public void testPlayerStartsUnprotected() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        assertFalse(player.isProtected());
    }
    
    @Test
    @DisplayName("Player can be protected")
    public void testPlayerCanBeProtected() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        player.setProtected(true);
        assertTrue(player.isProtected());
    }
    
    @Test
    @DisplayName("Player protection can be removed")
    public void testPlayerProtectionRemoval() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        player.setProtected(true);
        assertTrue(player.isProtected());
        
        player.setProtected(false);
        assertFalse(player.isProtected());
    }
    
    @Test
    @DisplayName("Player protection is independent")
    public void testPlayerProtectionIndependent() {
        Player player1 = new Player("p1", "Alice");
        player1.setRole(villagerRole);
        Player player2 = new Player("p2", "Bob");
        player2.setRole(werewolfRole);
        
        player1.setProtected(true);
        assertTrue(player1.isProtected());
        assertFalse(player2.isProtected());
    }
    
    @Test
    @DisplayName("Dead player can still be protected")
    public void testDeadPlayerCanBeProtected() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        player.setAlive(false);
        player.setProtected(true);
        
        assertFalse(player.isAlive());
        assertTrue(player.isProtected());
    }
    
    @Test
    @DisplayName("Protected player can still die independently")
    public void testProtectedPlayerCanDieIndependently() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        player.setProtected(true);
        player.setAlive(false);
        
        assertTrue(player.isProtected());
        assertFalse(player.isAlive());
    }
    
    // ============== MULTI-STATE TESTS ==============
    
    @Test
    @DisplayName("Player state changes do not affect other players")
    public void testPlayerStateChangeIndependence() {
        Player alice = new Player("p1", "Alice");
        alice.setRole(villagerRole);
        Player bob = new Player("p2", "Bob");
        bob.setRole(werewolfRole);
        Player carol = new Player("p3", "Carol");
        carol.setRole(medicRole);
        
        // Modify alice
        alice.setAlive(false);
        alice.setProtected(true);
        alice.setRole(medicRole);
        
        // Verify bob and carol are unchanged
        assertTrue(bob.isAlive());
        assertFalse(bob.isProtected());
        assertEquals(werewolfRole, bob.getRole());
        
        assertTrue(carol.isAlive());
        assertFalse(carol.isProtected());
        assertEquals(medicRole, carol.getRole());
    }
    
    @Test
    @DisplayName("Player can track death and protection simultaneously")
    public void testPlayerDeadAndProtected() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        
        // Set both states
        player.setAlive(false);
        player.setProtected(true);
        
        assertFalse(player.isAlive());
        assertTrue(player.isProtected());
        
        // Reset both states
        player.setAlive(true);
        player.setProtected(false);
        
        assertTrue(player.isAlive());
        assertFalse(player.isProtected());
    }
    
    @Test
    @DisplayName("Player metadata is consistent")
    public void testPlayerConsistency() {
        player = new Player("player1", "Alice");
        player.setRole(villagerRole);
        
        // Multiple checks should return consistent values
        for (int i = 0; i < 5; i++) {
            assertEquals("player1", player.getId());
            assertEquals("Alice", player.getUsername());
            assertEquals(villagerRole, player.getRole());
        }
    }
    
    // ============== EDGE CASES ==============
    
    @Test
    @DisplayName("Player with special characters in username")
    public void testPlayerSpecialCharacters() {
        player = new Player("p1", "Alice@#$%");
        player.setRole(villagerRole);
        assertEquals("Alice@#$%", player.getUsername());
    }
    
    @Test
    @DisplayName("Player with empty username")
    public void testPlayerEmptyUsername() {
        player = new Player("p1", "");
        player.setRole(villagerRole);
        assertEquals("", player.getUsername());
    }
    
    @Test
    @DisplayName("Multiple players with same role")
    public void testMultiplePlayersWithSameRole() {
        Player player1 = new Player("p1", "Alice");
        player1.setRole(villagerRole);
        Player player2 = new Player("p2", "Bob");
        player2.setRole(villagerRole);
        Player player3 = new Player("p3", "Carol");
        player3.setRole(villagerRole);
        
        assertEquals(villagerRole, player1.getRole());
        assertEquals(villagerRole, player2.getRole());
        assertEquals(villagerRole, player3.getRole());
    }
}
