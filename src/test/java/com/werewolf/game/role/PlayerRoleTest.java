package com.werewolf.game.role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerRole Tests")
public class PlayerRoleTest {
    
    // ============== VILLAGER ROLE TESTS ==============
    
    @Test
    @DisplayName("VillagerRole has correct name")
    public void testVillagerRoleName() {
        PlayerRole role = new VillagerRole();
        assertEquals("Villager", role.getName());
    }
    
    @Test
    @DisplayName("VillagerRole can only vote")
    public void testVillagerCanVote() {
        PlayerRole role = new VillagerRole();
        assertTrue(role.canPerform("VOTE"));
    }
    
    @Test
    @DisplayName("VillagerRole cannot kill")
    public void testVillagerCannotKill() {
        PlayerRole role = new VillagerRole();
        assertFalse(role.canPerform("KILL"));
    }
    
    @Test
    @DisplayName("VillagerRole cannot protect")
    public void testVillagerCannotProtect() {
        PlayerRole role = new VillagerRole();
        assertFalse(role.canPerform("HEAL"));
    }
    
    @Test
    @DisplayName("VillagerRole cannot investigate")
    public void testVillagerCannotInvestigate() {
        PlayerRole role = new VillagerRole();
        assertFalse(role.canPerform("INVESTIGATE"));
    }
    
    @Test
    @DisplayName("VillagerRole getAllowedActions")
    public void testVillagerAllowedActions() {
        PlayerRole role = new VillagerRole();
        List<String> actions = role.getAllowedActions();
        
        assertEquals(1, actions.size());
        assertTrue(actions.contains("VOTE"));
    }
    
    // ============== WEREWOLF ROLE TESTS ==============
    
    @Test
    @DisplayName("WerewolfRole has correct name")
    public void testWerewolfRoleName() {
        PlayerRole role = new WerewolfRole();
        assertEquals("Werewolf", role.getName());
    }
    
    @Test
    @DisplayName("WerewolfRole can kill")
    public void testWerewolfCanKill() {
        PlayerRole role = new WerewolfRole();
        assertTrue(role.canPerform("KILL"));
    }
    
    @Test
    @DisplayName("WerewolfRole can vote")
    public void testWerewolfCanVote() {
        PlayerRole role = new WerewolfRole();
        assertTrue(role.canPerform("VOTE"));
    }
    
    @Test
    @DisplayName("WerewolfRole cannot protect")
    public void testWerewolfCannotProtect() {
        PlayerRole role = new WerewolfRole();
        assertFalse(role.canPerform("HEAL"));
    }
    
    @Test
    @DisplayName("WerewolfRole cannot investigate")
    public void testWerewolfCannotInvestigate() {
        PlayerRole role = new WerewolfRole();
        assertFalse(role.canPerform("INVESTIGATE"));
    }
    
    @Test
    @DisplayName("WerewolfRole getAllowedActions")
    public void testWerewolfAllowedActions() {
        PlayerRole role = new WerewolfRole();
        List<String> actions = role.getAllowedActions();
        
        assertEquals(2, actions.size());
        assertTrue(actions.contains("KILL"));
        assertTrue(actions.contains("VOTE"));
    }
    
    // ============== MEDIC ROLE TESTS ==============
    
    @Test
    @DisplayName("MedicRole has correct name")
    public void testMedicRoleName() {
        PlayerRole role = new MedicRole();
        assertEquals("Medic", role.getName());
    }
    
    @Test
    @DisplayName("MedicRole can protect")
    public void testMedicCanProtect() {
        PlayerRole role = new MedicRole();
        assertTrue(role.canPerform("HEAL"));
    }
    
    @Test
    @DisplayName("MedicRole can vote")
    public void testMedicCanVote() {
        PlayerRole role = new MedicRole();
        assertTrue(role.canPerform("VOTE"));
    }
    
    @Test
    @DisplayName("MedicRole cannot kill")
    public void testMedicCannotKill() {
        PlayerRole role = new MedicRole();
        assertFalse(role.canPerform("KILL"));
    }
    
    @Test
    @DisplayName("MedicRole cannot investigate")
    public void testMedicCannotInvestigate() {
        PlayerRole role = new MedicRole();
        assertFalse(role.canPerform("PEEK"));
    }
    
    @Test
    @DisplayName("MedicRole getAllowedActions")
    public void testMedicAllowedActions() {
        PlayerRole role = new MedicRole();
        List<String> actions = role.getAllowedActions();
        
        assertEquals(2, actions.size());
        assertTrue(actions.contains("HEAL"));
        assertTrue(actions.contains("VOTE"));
    }
    
    // ============== SEER ROLE TESTS ==============
    
    @Test
    @DisplayName("SeerRole has correct name")
    public void testSeerRoleName() {
        PlayerRole role = new SeerRole();
        assertEquals("Seer", role.getName());
    }
    
    @Test
    @DisplayName("SeerRole can investigate")
    public void testSeerCanInvestigate() {
        PlayerRole role = new SeerRole();
        assertTrue(role.canPerform("PEEK"));
    }
    
    @Test
    @DisplayName("SeerRole can vote")
    public void testSeerCanVote() {
        PlayerRole role = new SeerRole();
        assertTrue(role.canPerform("VOTE"));
    }
    
    @Test
    @DisplayName("SeerRole cannot kill")
    public void testSeerCannotKill() {
        PlayerRole role = new SeerRole();
        assertFalse(role.canPerform("KILL"));
    }
    
    @Test
    @DisplayName("SeerRole cannot protect")
    public void testSeerCannotProtect() {
        PlayerRole role = new SeerRole();
        assertFalse(role.canPerform("HEAL"));
    }
    
    @Test
    @DisplayName("SeerRole getAllowedActions")
    public void testSeerAllowedActions() {
        PlayerRole role = new SeerRole();
        List<String> actions = role.getAllowedActions();
        
        assertEquals(2, actions.size());
        assertTrue(actions.contains("PEEK"));
        assertTrue(actions.contains("VOTE"));
    }
    
    // ============== CROSS-ROLE COMPARISON TESTS ==============
    
    @Test
    @DisplayName("Different roles have different permissions")
    public void testDifferentRolesHaveDifferentPermissions() {
        PlayerRole villager = new VillagerRole();
        PlayerRole werewolf = new WerewolfRole();
        PlayerRole medic = new MedicRole();
        PlayerRole seer = new SeerRole();
        
        // Only werewolf can kill
        assertTrue(werewolf.canPerform("KILL"));
        assertFalse(villager.canPerform("KILL"));
        assertFalse(medic.canPerform("KILL"));
        assertFalse(seer.canPerform("KILL"));
        
        // Only medic can protect
        assertTrue(medic.canPerform("HEAL"));
        assertFalse(villager.canPerform("HEAL"));
        assertFalse(werewolf.canPerform("HEAL"));
        assertFalse(seer.canPerform("HEAL"));
        
        // Only seer can investigate
        assertTrue(seer.canPerform("PEEK"));
        assertFalse(villager.canPerform("PEEK"));
        assertFalse(werewolf.canPerform("PEEK"));
        assertFalse(medic.canPerform("PEEK"));
        
        // All can vote
        assertTrue(villager.canPerform("VOTE"));
        assertTrue(werewolf.canPerform("VOTE"));
        assertTrue(medic.canPerform("VOTE"));
        assertTrue(seer.canPerform("VOTE"));
    }
}
