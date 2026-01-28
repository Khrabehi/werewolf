import domain.model.Game;
import domain.model.Player;
import domain.model.GameConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Example unit tests for the Game class
 * Demonstrates the testability of the new code
 */
class GameTest {
    
    private Game game;
    private GameConfiguration config;

    @BeforeEach
    void setUp() {
        config = GameConfiguration.defaultConfiguration();
        game = new Game(config.getMinPlayers(), config.getMaxPlayers());
    }

    @Test
    void testAddPlayer() {
        Player player = new Player("Alice");
        game.addPlayer(player);
        
        assertEquals(1, game.getPlayers().size());
        assertEquals(player, game.getAdmin());
    }

    @Test
    void testCannotStartWithoutEnoughPlayers() {
        game.addPlayer(new Player("Alice"));
        game.addPlayer(new Player("Bob"));
        
        assertFalse(game.canStart());
    }

    @Test
    void testCanStartWithMinimumPlayers() {
        for (int i = 0; i < 4; i++) {
            game.addPlayer(new Player("Player" + i));
        }
        
        assertTrue(game.canStart());
    }

    @Test
    void testStartGameThrowsExceptionWhenNotReady() {
        game.addPlayer(new Player("Alice"));
        
        assertThrows(IllegalStateException.class, () -> game.start());
    }

    @Test
    void testAdminReassignmentWhenAdminLeaves() {
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");
        
        game.addPlayer(alice);
        game.addPlayer(bob);
        
        assertEquals(alice, game.getAdmin());
        
        game.removePlayer(alice);
        
        assertEquals(bob, game.getAdmin());
    }

    @Test
    void testFindPlayerByPseudo() {
        Player alice = new Player("Alice");
        game.addPlayer(alice);
        
        assertTrue(game.findPlayerByPseudo("Alice").isPresent());
        assertTrue(game.findPlayerByPseudo("alice").isPresent()); // Case insensitive
        assertFalse(game.findPlayerByPseudo("Bob").isPresent());
    }

    @Test
    void testCannotAddPlayerWhenGameStarted() {
        for (int i = 0; i < 4; i++) {
            game.addPlayer(new Player("Player" + i));
        }
        game.start();
        
        assertThrows(IllegalStateException.class, 
            () -> game.addPlayer(new Player("Late")));
    }

    @Test
    void testGetAlivePlayersFiltersDeadPlayers() {
        Player alice = new Player("Alice");
        Player bob = new Player("Bob");
        
        game.addPlayer(alice);
        game.addPlayer(bob);
        
        assertEquals(2, game.getAlivePlayers().size());
        
        alice.kill();
        
        assertEquals(1, game.getAlivePlayers().size());
        assertTrue(game.getAlivePlayers().contains(bob));
    }
}
