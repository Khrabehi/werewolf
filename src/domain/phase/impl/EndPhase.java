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
 * End game phase
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
        
        String winner = werewolvesAlive == 0 ? "Villagers" : "Werewolves";
        
        events.add(new GameEndEvent(winner));
        events.add(new MessageEvent("The game is over! The " + winner + " have won!"));
        
        return events;
    }

    @Override
    public boolean canEnd(Game game) {
        return true;
    }

    @Override
    public GamePhase end(Game game) {
        return this; // Final phase
    }

    @Override
    public GamePhase next(Game game) {
        return this; // No next phase
    }
}
