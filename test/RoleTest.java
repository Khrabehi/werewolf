import domain.model.Player;
import domain.role.impl.WerewolfRole;
import domain.role.impl.VillagerRole;
import domain.role.Team;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests pour les rôles
 */
class RoleTest {

    @Test
    void testWerewolfRoleProperties() {
        WerewolfRole werewolf = new WerewolfRole();
        
        assertEquals("Loup-Garou", werewolf.getName());
        assertEquals(Team.WEREWOLVES, werewolf.getTeam());
        assertTrue(werewolf.canActDuringPhase("NIGHT"));
        assertFalse(werewolf.canActDuringPhase("DAY"));
    }

    @Test
    void testVillagerRoleProperties() {
        VillagerRole villager = new VillagerRole();
        
        assertEquals("Villageois", villager.getName());
        assertEquals(Team.VILLAGERS, villager.getTeam());
        assertFalse(villager.canActDuringPhase("NIGHT"));
        assertTrue(villager.canActDuringPhase("DAY"));
    }

    @Test
    void testPlayerRoleAssignment() {
        Player player = new Player("Alice");
        WerewolfRole role = new WerewolfRole();
        
        player.assignRole(role);
        
        assertEquals(role, player.getRole());
        assertEquals(Team.WEREWOLVES, player.getRole().getTeam());
    }
}
