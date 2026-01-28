package unit;

import application.service.CommandService;
import application.service.GameService;
import application.service.VoteService;
import application.event.GameEvent;
import domain.model.GameConfiguration;
import domain.model.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CommandService
 */
public class CommandServiceTest {
    
    private CommandService commandService;
    private GameService gameService;
    private Player player;
    
    @BeforeEach
    void setUp() {
        GameConfiguration config = new GameConfiguration(4, 10, 1);
        gameService = new GameService(config);
        VoteService voteService = new VoteService(gameService.getGame());
        commandService = new CommandService(gameService, voteService);
        
        player = gameService.addPlayer();
    }
    
    @Test
    @DisplayName("PSEUDO command changes the player's pseudo")
    void testPseudoCommand() {
        List<GameEvent> events = commandService.handleCommand("PSEUDO TestPlayer", player);
        
        assertEquals("TestPlayer", player.getPseudo());
        assertFalse(events.isEmpty());
        assertTrue(events.get(0).getMessage().contains("TestPlayer"));
        
        System.out.println("PSEUDO correctly changes the pseudo");
    }
    
    @Test
    @DisplayName("Unknown command returns an error message")
    void testUnknownCommand() {
        List<GameEvent> events = commandService.handleCommand("INVALID_COMMAND", player);
        
        assertFalse(events.isEmpty());
        assertTrue(events.get(0).getMessage().contains("Commande inconnue"));
        
        System.out.println("Unknown command handled correctly");
    }
    
    @Test
    @DisplayName("Empty command returns an error message")
    void testEmptyCommand() {
        List<GameEvent> events = commandService.handleCommand("", player);
        
        assertFalse(events.isEmpty());
        assertTrue(events.get(0).getMessage().contains("Commande inconnue"));
        
        System.out.println("Empty command handled correctly");
    }
    
    @Test
    @DisplayName("START without being admin fails")
    void testStartWithoutAdmin() {
        // Add a 2nd player so that player is no longer admin
        Player admin = gameService.getGame().getAdmin();
        Player notAdmin = player.equals(admin) ? gameService.addPlayer() : player;
        
        if (!notAdmin.equals(admin)) {
            commandService.handleCommand("PSEUDO Player1", admin);
            commandService.handleCommand("PSEUDO Player2", notAdmin);
            
            List<GameEvent> events = commandService.handleCommand("START", notAdmin);
            
            assertFalse(gameService.getGame().isStarted());
            assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("administrateur")));
            
            System.out.println("Only the admin can START");
        }
    }
}
