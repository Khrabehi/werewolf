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
 * Tests unitaires pour CommandService
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
    @DisplayName("Commande PSEUDO change le pseudo du joueur")
    void testPseudoCommand() {
        List<GameEvent> events = commandService.handleCommand("PSEUDO TestPlayer", player);
        
        assertEquals("TestPlayer", player.getPseudo());
        assertFalse(events.isEmpty());
        assertTrue(events.get(0).getMessage().contains("TestPlayer"));
        
        System.out.println("✅ PSEUDO change bien le pseudo");
    }
    
    @Test
    @DisplayName("Commande inconnue retourne un message d'erreur")
    void testUnknownCommand() {
        List<GameEvent> events = commandService.handleCommand("INVALID_COMMAND", player);
        
        assertFalse(events.isEmpty());
        assertTrue(events.get(0).getMessage().contains("Commande inconnue"));
        
        System.out.println("✅ Commande inconnue gérée correctement");
    }
    
    @Test
    @DisplayName("Commande vide retourne un message d'erreur")
    void testEmptyCommand() {
        List<GameEvent> events = commandService.handleCommand("", player);
        
        assertFalse(events.isEmpty());
        assertTrue(events.get(0).getMessage().contains("Commande inconnue"));
        
        System.out.println("✅ Commande vide gérée correctement");
    }
    
    @Test
    @DisplayName("START sans être admin échoue")
    void testStartWithoutAdmin() {
        // Ajouter un 2ème joueur pour que player ne soit plus admin
        Player admin = gameService.getGame().getAdmin();
        Player notAdmin = player.equals(admin) ? gameService.addPlayer() : player;
        
        if (!notAdmin.equals(admin)) {
            commandService.handleCommand("PSEUDO Player1", admin);
            commandService.handleCommand("PSEUDO Player2", notAdmin);
            
            List<GameEvent> events = commandService.handleCommand("START", notAdmin);
            
            assertFalse(gameService.getGame().isStarted());
            assertTrue(events.stream().anyMatch(e -> e.getMessage().contains("administrateur")));
            
            System.out.println("✅ Seul l'admin peut START");
        }
    }
}
