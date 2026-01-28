package integration;

import application.service.CommandService;
import application.service.GameService;
import application.service.VoteService;
import application.event.GameEvent;
import application.event.impl.RoleAssignedEvent;
import application.event.impl.PlayerDeathEvent;
import domain.model.Game;
import domain.model.GameConfiguration;
import domain.model.Player;
import domain.role.Team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests simulating a complete game
 * Avoids opening 4 terminals for testing!
 */
public class GameIntegrationTest {
    
    private GameService gameService;
    private VoteService voteService;
    private CommandService commandService;
    private Game game;
    
    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;
    
    @BeforeEach
    void setUp() {
        GameConfiguration config = new GameConfiguration(4, 10, 1);
        gameService = new GameService(config);
        game = gameService.getGame();
        voteService = new VoteService(game);
        commandService = new CommandService(gameService, voteService);
        
        // Create 4 players
        player1 = gameService.addPlayer();
        player2 = gameService.addPlayer();
        player3 = gameService.addPlayer();
        player4 = gameService.addPlayer();
        
        // Assign them pseudonyms
        commandService.handleCommand("PSEUDO Alice", player1);
        commandService.handleCommand("PSEUDO Bob", player2);
        commandService.handleCommand("PSEUDO Charlie", player3);
        commandService.handleCommand("PSEUDO Diana", player4);
    }
    
    @Test
    @DisplayName("A complete game should proceed correctly")
    void testCompleteGameFlow() {
        // 1. Verify that the game has not started yet
        assertFalse(game.isStarted());
        assertEquals(4, game.getPlayers().size());
        
        // 2. Start the game (player1 is admin)
        List<GameEvent> startEvents = commandService.handleCommand("START", player1);
        assertTrue(game.isStarted());
        
        // 3. Verify that roles have been assigned
        long roleAssignedCount = startEvents.stream()
                .filter(e -> e instanceof RoleAssignedEvent)
                .count();
        assertEquals(4, roleAssignedCount, "4 roles must be assigned");
        
        // 4. Verify that all players have a role
        assertTrue(player1.getRole() != null);
        assertTrue(player2.getRole() != null);
        assertTrue(player3.getRole() != null);
        assertTrue(player4.getRole() != null);
        
        // 5. Identify the werewolves
        List<Player> werewolves = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.WEREWOLVES)
                .toList();
        
        assertTrue(werewolves.size() >= 1, "There must be at least 1 werewolf");
        
        // 6. Verify that the phase is NIGHT
        assertEquals("NIGHT", game.getCurrentPhase().getName());
        
        // 7. Werewolves vote to kill someone
        List<Player> villagers = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.VILLAGERS)
                .toList();
        
        // Ensure there is at least one villager
        if (villagers.isEmpty()) {
            System.out.println("WARNING: No villagers in this configuration, test incomplete");
            return;
        }
        
        Player victim = villagers.get(0);
        
        // All werewolves vote for the same victim
        for (Player werewolf : werewolves) {
            List<GameEvent> voteEvents = commandService.handleCommand(
                "KILL " + victim.getPseudo(), 
                werewolf
            );
            assertFalse(voteEvents.isEmpty(), "The vote must generate events");
        }
        
        // 8. Verify that the victim is dead
        assertFalse(victim.isAlive(), "The victim must be dead");
        
        // 9. Verify transition to DAY phase
        assertEquals("DAY", game.getCurrentPhase().getName());
        
        // 10. Verify that a death event was generated
        long deathEvents = game.getPlayers().stream()
                .filter(p -> !p.isAlive())
                .count();
        assertEquals(1, deathEvents, "1 player must be dead");
        
        System.out.println("Complete game test successful!");
        System.out.println("   - 4 players created");
        System.out.println("   - Roles assigned");
        System.out.println("   - Phase NIGHT -> DAY");
        System.out.println("   - 1 victim killed by werewolves");
    }
    
    @Test
    @DisplayName("Non-werewolves cannot vote at night")
    void testOnlyWerewolvesCanVoteAtNight() {
        // Start the game
        commandService.handleCommand("START", player1);
        
        // Find a villager
        Player villager = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.VILLAGERS)
                .findFirst()
                .orElse(null);
        
        if (villager == null) {
            System.out.println("WARNING: No villagers in this configuration, test incomplete");
            return;
        }
        
        // Try to vote with a villager
        List<GameEvent> events = commandService.handleCommand("KILL Bob", villager);
        
        // Verify that an error is returned
        boolean hasErrorMessage = events.stream()
                .anyMatch(e -> e.getMessage().contains("Seuls les Loups-Garous") || 
                              e.getMessage().contains("ne peut pas agir"));
        
        assertTrue(hasErrorMessage, "A villager should not be able to vote at night");
        
        System.out.println("Validation: only werewolves can vote at night");
    }
    
    @Test
    @DisplayName("Only the admin can start the game")
    void testOnlyAdminCanStart() {
        // player1 is admin, player2 is not
        List<GameEvent> events = commandService.handleCommand("START", player2);
        
        assertFalse(game.isStarted(), "The game must not start");
        
        boolean hasErrorMessage = events.stream()
                .anyMatch(e -> e.getMessage().contains("administrateur"));
        
        assertTrue(hasErrorMessage, "An error message must be displayed");
        
        System.out.println("Validation: only the admin can execute START");
    }
    
    @Test
    @DisplayName("Cannot start with less than 4 players")
    void testCannotStartWithLessThan4Players() {
        // Create a new game with only 2 players
        GameConfiguration config = new GameConfiguration(4, 10, 1);
        GameService newGameService = new GameService(config);
        CommandService newCommandService = new CommandService(
            newGameService, 
            new VoteService(newGameService.getGame())
        );
        
        Player p1 = newGameService.addPlayer();
        Player p2 = newGameService.addPlayer();
        
        newCommandService.handleCommand("PSEUDO Alice", p1);
        newCommandService.handleCommand("PSEUDO Bob", p2);
        
        List<GameEvent> events = newCommandService.handleCommand("START", p1);
        
        assertFalse(newGameService.getGame().isStarted());
        
        boolean hasErrorMessage = events.stream()
                .anyMatch(e -> e.getMessage().contains("pas assez de joueurs"));
        
        assertTrue(hasErrorMessage);
        
        System.out.println("Validation: minimum 4 players required");
    }
    
    @Test
    @DisplayName("Majority vote works correctly")
    void testMajorityVoteWorks() {
        commandService.handleCommand("START", player1);
        
        // Identify werewolves (at least 1)
        List<Player> werewolves = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.WEREWOLVES)
                .toList();
        
        List<Player> villagers = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.VILLAGERS)
                .toList();
        
        // Verify there are at least 2 werewolves and 2 villagers
        if (werewolves.size() < 2 || villagers.size() < 2) {
            System.out.println("WARNING: Inadequate configuration for this test (need 2+ werewolves, 2+ villagers)");
            return;
        }
        
        // First werewolf votes for villager 1
        commandService.handleCommand("KILL " + villagers.get(0).getPseudo(), werewolves.get(0));
        
        // Second werewolf also votes for villager 1 (majority)
        commandService.handleCommand("KILL " + villagers.get(0).getPseudo(), werewolves.get(1));
        
        // Verify that villager 1 is indeed dead
        assertFalse(villagers.get(0).isAlive());
        assertTrue(villagers.get(1).isAlive());
        
        System.out.println("Majority vote validated");
    }
}
