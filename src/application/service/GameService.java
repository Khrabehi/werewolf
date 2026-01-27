package application.service;

import domain.model.Game;
import domain.model.Player;
import domain.model.GameConfiguration;
import domain.role.Role;
import domain.role.RoleFactory;
import domain.phase.impl.NightPhase;
import application.event.GameEvent;
import application.event.impl.MessageEvent;
import application.event.impl.RoleAssignedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion du jeu - Principe SRP
 * Responsable de la logique métier du jeu
 */
public class GameService {
    private final Game game;
    private final GameConfiguration configuration;

    public GameService(GameConfiguration configuration) {
        this.configuration = configuration;
        this.game = new Game(configuration.getMinPlayers(), configuration.getMaxPlayers());
    }

    public Game getGame() {
        return game;
    }

    /**
     * Ajoute un joueur à la partie
     */
    public Player addPlayer() {
        Player player = new Player(null);
        game.addPlayer(player);
        return player;
    }

    /**
     * Supprime un joueur de la partie
     */
    public void removePlayer(Player player) {
        game.removePlayer(player);
    }

    /**
     * Démarre la partie
     */
    public List<GameEvent> startGame() {
        List<GameEvent> events = new ArrayList<>();
        
        if (!game.canStart()) {
            events.add(new MessageEvent("Impossible de démarrer la partie"));
            return events;
        }
        
        game.start();
        
        // Assigner les rôles
        events.addAll(assignRoles());
        
        // Démarrer la première phase (nuit)
        NightPhase firstPhase = new NightPhase();
        game.setCurrentPhase(firstPhase);
        events.addAll(firstPhase.start(game));
        
        return events;
    }

    /**
     * Assigne les rôles aux joueurs
     */
    private List<GameEvent> assignRoles() {
        List<GameEvent> events = new ArrayList<>();
        int playerCount = game.getPlayers().size();
        int werewolvesCount = configuration.calculateWerewolvesCount(playerCount);
        
        List<Role> roles = RoleFactory.distributeRoles(playerCount, werewolvesCount);
        
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            Role role = roles.get(i);
            player.assignRole(role);
            
            events.add(new RoleAssignedEvent(player.getId(), role.getName()));
        }
        
        events.add(new MessageEvent("Les rôles ont été distribués."));
        return events;
    }

    /**
     * Termine la phase actuelle et passe à la suivante
     */
    public List<GameEvent> advancePhase() {
        List<GameEvent> events = new ArrayList<>();
        
        if (game.getCurrentPhase() == null) {
            return events;
        }
        
        var nextPhase = game.getCurrentPhase().next(game);
        game.setCurrentPhase(nextPhase);
        events.addAll(nextPhase.start(game));
        
        return events;
    }

    /**
     * Retourne la liste des pseudos des joueurs
     */
    public List<String> getPlayerList() {
        return game.getPlayers().stream()
                .filter(p -> p.getPseudo() != null)
                .map(Player::getPseudo)
                .toList();
    }
}
