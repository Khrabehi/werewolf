package domain.phase.impl;

import domain.model.Game;
import domain.phase.GamePhase;
import domain.role.Team;
import application.event.GameEvent;
import application.event.impl.GameEndEvent;
import application.event.impl.MessageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase de fin de jeu
 */
public class EndPhase implements GamePhase {

    @Override
    public String getName() {
        return "END";
    }

    @Override
    public List<GameEvent> start(Game game) {
        List<GameEvent> events = new ArrayList<>();
        
        long werewolvesAlive = game.getAlivePlayers().stream()
                .filter(p -> p.getRole() != null && p.getRole().getTeam() == Team.WEREWOLVES)
                .count();
        
        String winner = werewolvesAlive == 0 ? "Villageois" : "Loups-Garous";
        
        events.add(new GameEndEvent(winner));
        events.add(new MessageEvent("La partie est terminée ! Les " + winner + " ont gagné !"));
        
        return events;
    }

    @Override
    public boolean canEnd(Game game) {
        return true;
    }

    @Override
    public GamePhase end(Game game) {
        return this; // Phase finale
    }

    @Override
    public GamePhase next(Game game) {
        return this; // Pas de phase suivante
    }
}
