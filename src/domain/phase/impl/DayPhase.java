package domain.phase.impl;

import domain.model.Game;
import domain.phase.GamePhase;
import application.event.GameEvent;
import application.event.impl.PhaseStartEvent;
import application.event.impl.MessageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Day phase - State Pattern
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
        events.add(new MessageEvent("The sun rises over the village..."));
        events.add(new MessageEvent("The villagers can deliberate and vote."));
        return events;
    }

    @Override
    public boolean canEnd(Game game) {
        // The day phase can end after a vote or timeout
        return true;
    }

    @Override
    public GamePhase end(Game game) {
        game.resetAllVotes();
        return next(game);
    }

    @Override
    public GamePhase next(Game game) {
        // Check victory conditions
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
        
        // The game ends if all werewolves or all villagers are dead
        return werewolvesAlive == 0 || villagersAlive == 0;
    }
}
