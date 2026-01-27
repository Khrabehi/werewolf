package domain.phase.impl;

import domain.model.Game;
import domain.model.Player;
import domain.phase.GamePhase;
import domain.role.Team;
import application.event.GameEvent;
import application.event.impl.PhaseStartEvent;
import application.event.impl.MessageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase de nuit - State Pattern
 */
public class NightPhase implements GamePhase {

    @Override
    public String getName() {
        return "NIGHT";
    }

    @Override
    public List<GameEvent> start(Game game) {
        List<GameEvent> events = new ArrayList<>();
        events.add(new PhaseStartEvent("NIGHT"));
        events.add(new MessageEvent("La nuit tombe sur le village..."));
        
        // Notifier les loups-garous
        long werewolvesCount = game.getAlivePlayers().stream()
                .filter(p -> p.getRole() != null && p.getRole().getTeam() == Team.WEREWOLVES)
                .count();
        
        if (werewolvesCount > 0) {
            events.add(new MessageEvent("Les Loups-Garous se réveillent. Envoyez KILL <pseudo> pour voter."));
        }
        
        return events;
    }

    @Override
    public boolean canEnd(Game game) {
        // La phase peut se terminer quand tous les loups ont voté
        // (vérifié par le VoteService)
        return true;
    }

    @Override
    public GamePhase end(Game game) {
        game.resetAllVotes();
        return next(game);
    }

    @Override
    public GamePhase next(Game game) {
        return new DayPhase();
    }
}
