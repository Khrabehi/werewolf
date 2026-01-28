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
 * Game management service - SRP Principle
 * Responsible for game business logic
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
     * Adds a player to the game
     */
    public Player addPlayer() {
        Player player = new Player(null);
        game.addPlayer(player);
        return player;
    }

    /**
     * Removes a player from the game
     */
    public void removePlayer(Player player) {
        game.removePlayer(player);
        // The Game class should handle admin reassignment in its removePlayer method
    }

    /**
     * Starts the game
     */
    public List<GameEvent> startGame() {
        List<GameEvent> events = new ArrayList<>();
        
        if (!game.canStart()) {
            events.add(new MessageEvent("Cannot start the game"));
            return events;
        }
        
        game.start();
        
        // Assign roles
        events.addAll(assignRoles());
        
        // Start the first phase (night)
        NightPhase firstPhase = new NightPhase();
        game.setCurrentPhase(firstPhase);
        events.addAll(firstPhase.start(game));
        
        return events;
    }

    /**
     * Assigns roles to players
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
        
        events.add(new MessageEvent("Roles have been distributed."));
        return events;
    }

    /**
     * Ends the current phase and moves to the next
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
     * Returns the list of player pseudos
     */
    public List<String> getPlayerList() {
        return game.getPlayers().stream()
                .filter(p -> p.getPseudo() != null)
                .map(Player::getPseudo)
                .toList();
    }
}
