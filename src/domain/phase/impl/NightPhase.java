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
 * Night phase - State Pattern
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
        events.add(new MessageEvent("Night falls over the village..."));
        
        // Notify the werewolves
        long werewolvesCount = game.getAlivePlayers().stream()
                .filter(p -> p.getRole() != null && p.getRole().getTeam() == Team.WEREWOLVES)
                .count();
        
        if (werewolvesCount > 0) {
            events.add(new MessageEvent("The Werewolves awaken. Send KILL <pseudo> to vote."));
        }
        
        return events;
    }

    @Override
    public boolean canEnd(Game game) {
        // The phase can end when all werewolves have voted
        // (checked by VoteService)
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
