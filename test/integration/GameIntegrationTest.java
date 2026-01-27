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
 * Tests d'intégration simulant une partie complète
 * Évite d'ouvrir 4 terminaux pour tester !
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
        
        // Créer 4 joueurs
        player1 = gameService.addPlayer();
        player2 = gameService.addPlayer();
        player3 = gameService.addPlayer();
        player4 = gameService.addPlayer();
        
        // Leur donner des pseudos
        commandService.handleCommand("PSEUDO Alice", player1);
        commandService.handleCommand("PSEUDO Bob", player2);
        commandService.handleCommand("PSEUDO Charlie", player3);
        commandService.handleCommand("PSEUDO Diana", player4);
    }
    
    @Test
    @DisplayName("Une partie complète doit se dérouler correctement")
    void testCompleteGameFlow() {
        // 1. Vérifier que la partie n'a pas encore démarré
        assertFalse(game.isStarted());
        assertEquals(4, game.getPlayers().size());
        
        // 2. Démarrer la partie (player1 est admin)
        List<GameEvent> startEvents = commandService.handleCommand("START", player1);
        assertTrue(game.isStarted());
        
        // 3. Vérifier que les rôles ont été assignés
        long roleAssignedCount = startEvents.stream()
                .filter(e -> e instanceof RoleAssignedEvent)
                .count();
        assertEquals(4, roleAssignedCount, "4 rôles doivent être assignés");
        
        // 4. Vérifier que tous les joueurs ont un rôle
        assertTrue(player1.getRole() != null);
        assertTrue(player2.getRole() != null);
        assertTrue(player3.getRole() != null);
        assertTrue(player4.getRole() != null);
        
        // 5. Identifier les loups-garous
        List<Player> werewolves = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.WEREWOLVES)
                .toList();
        
        assertTrue(werewolves.size() >= 1, "Il doit y avoir au moins 1 loup-garou");
        
        // 6. Vérifier que la phase est NIGHT
        assertEquals("NIGHT", game.getCurrentPhase().getName());
        
        // 7. Les loups votent pour tuer quelqu'un
        List<Player> villagers = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.VILLAGERS)
                .toList();
        
        // S'assurer qu'il y a au moins un villageois
        if (villagers.isEmpty()) {
            System.out.println("⚠️  Aucun villageois dans cette configuration, test incomplet");
            return;
        }
        
        Player victim = villagers.get(0);
        
        // Tous les loups votent pour la même victime
        for (Player werewolf : werewolves) {
            List<GameEvent> voteEvents = commandService.handleCommand(
                "KILL " + victim.getPseudo(), 
                werewolf
            );
            assertFalse(voteEvents.isEmpty(), "Le vote doit générer des événements");
        }
        
        // 8. Vérifier que la victime est morte
        assertFalse(victim.isAlive(), "La victime doit être morte");
        
        // 9. Vérifier le passage à la phase DAY
        assertEquals("DAY", game.getCurrentPhase().getName());
        
        // 10. Vérifier qu'un événement de mort a été généré
        long deathEvents = game.getPlayers().stream()
                .filter(p -> !p.isAlive())
                .count();
        assertEquals(1, deathEvents, "1 joueur doit être mort");
        
        System.out.println("✅ Test de partie complète réussi !");
        System.out.println("   - 4 joueurs créés");
        System.out.println("   - Rôles assignés");
        System.out.println("   - Phase NIGHT → DAY");
        System.out.println("   - 1 victime tuée par les loups");
    }
    
    @Test
    @DisplayName("Les non-loups ne peuvent pas voter la nuit")
    void testOnlyWerewolvesCanVoteAtNight() {
        // Démarrer la partie
        commandService.handleCommand("START", player1);
        
        // Trouver un villageois
        Player villager = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.VILLAGERS)
                .findFirst()
                .orElse(null);
        
        if (villager == null) {
            System.out.println("⚠️  Pas de villageois dans cette configuration, test incomplet");
            return;
        }
        
        // Essayer de voter avec un villageois
        List<GameEvent> events = commandService.handleCommand("KILL Bob", villager);
        
        // Vérifier qu'une erreur est retournée
        boolean hasErrorMessage = events.stream()
                .anyMatch(e -> e.getMessage().contains("Seuls les Loups-Garous") || 
                              e.getMessage().contains("ne peut pas agir"));
        
        assertTrue(hasErrorMessage, "Un villageois ne devrait pas pouvoir voter la nuit");
        
        System.out.println("✅ Validation: seuls les loups peuvent voter la nuit");
    }
    
    @Test
    @DisplayName("Seul l'admin peut démarrer la partie")
    void testOnlyAdminCanStart() {
        // player1 est admin, player2 ne l'est pas
        List<GameEvent> events = commandService.handleCommand("START", player2);
        
        assertFalse(game.isStarted(), "La partie ne doit pas démarrer");
        
        boolean hasErrorMessage = events.stream()
                .anyMatch(e -> e.getMessage().contains("administrateur"));
        
        assertTrue(hasErrorMessage, "Un message d'erreur doit être affiché");
        
        System.out.println("✅ Validation: seul l'admin peut lancer START");
    }
    
    @Test
    @DisplayName("Impossible de démarrer avec moins de 4 joueurs")
    void testCannotStartWithLessThan4Players() {
        // Créer une nouvelle partie avec seulement 2 joueurs
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
        
        System.out.println("✅ Validation: minimum 4 joueurs requis");
    }
    
    @Test
    @DisplayName("Le vote majoritaire fonctionne correctement")
    void testMajorityVoteWorks() {
        commandService.handleCommand("START", player1);
        
        // Identifier les loups (au moins 1)
        List<Player> werewolves = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.WEREWOLVES)
                .toList();
        
        List<Player> villagers = game.getAlivePlayers().stream()
                .filter(p -> p.getRole().getTeam() == Team.VILLAGERS)
                .toList();
        
        // Vérifier qu'il y a au moins 2 loups et 2 villageois
        if (werewolves.size() < 2 || villagers.size() < 2) {
            System.out.println("⚠️  Configuration inadéquate pour ce test (besoin 2+ loups, 2+ villageois)");
            return;
        }
        
        // Premier loup vote pour villageois 1
        commandService.handleCommand("KILL " + villagers.get(0).getPseudo(), werewolves.get(0));
        
        // Deuxième loup vote pour villageois 1 aussi (majorité)
        commandService.handleCommand("KILL " + villagers.get(0).getPseudo(), werewolves.get(1));
        
        // Vérifier que c'est bien villageois 1 qui est mort
        assertFalse(villagers.get(0).isAlive());
        assertTrue(villagers.get(1).isAlive());
        
        System.out.println("✅ Vote majoritaire validé");
    }
}
