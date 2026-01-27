package domain.phase.impl;

import domain.model.Game;
import domain.phase.GamePhase;
import application.event.GameEvent;
import application.event.impl.PhaseStartEvent;
import application.event.impl.MessageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase de jour - State Pattern
 */
public class DayPhase implements GamePhase {

    @Override
    public String getName() {
        return "DAY";
    }

    @Override
    public List<GameEvent> start(Game game) {
        List<GameEvent> events = new ArrayList<>();
        events.add(new PhaseStartEvent("DAY"));
        events.add(new MessageEvent("Le jour se lève sur le village..."));
        events.add(new MessageEvent("Les villageois peuvent délibérer et voter."));
        return events;
    }

    @Override
    public boolean canEnd(Game game) {
        // La phase jour peut se terminer après un vote ou un timeout
        return true;
    }

    @Override
    public GamePhase end(Game game) {
        game.resetAllVotes();
        return next(game);
    }

    @Override
    public GamePhase next(Game game) {
        // Vérifier les conditions de victoire
        if (checkGameEnd(game)) {
            return new EndPhase();
        }
        return new NightPhase();
    }

    private boolean checkGameEnd(Game game) {
        long werewolvesAlive = game.getAlivePlayers().stream()
                .filter(p -> p.getRole() != null && p.getRole().getTeam() == domain.role.Team.WEREWOLVES)
                .count();
        
        long villagersAlive = game.getAlivePlayers().stream()
                .filter(p -> p.getRole() != null && p.getRole().getTeam() == domain.role.Team.VILLAGERS)
                .count();
        
        // Le jeu se termine si tous les loups ou tous les villageois sont morts
        return werewolvesAlive == 0 || villagersAlive == 0;
    }
}
