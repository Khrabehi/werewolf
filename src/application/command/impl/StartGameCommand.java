package application.command.impl;

import application.command.GameCommand;
import application.event.GameEvent;
import application.event.impl.MessageEvent;
import domain.model.Game;
import domain.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to start the game - Command Pattern
 */
public class StartGameCommand implements GameCommand {

    @Override
    public String getName() {
        return "START";
    }

    @Override
    public boolean canExecute(Player executor, Game game) {
        if (game.isStarted()) {
            return false;
        }
        if (!game.getAdmin().equals(executor)) {
            return false;
        }
        return game.canStart();
    }

    @Override
    public List<GameEvent> execute(Player executor, Game game) {
        List<GameEvent> events = new ArrayList<>();
        
        if (!canExecute(executor, game)) {
            if (!game.getAdmin().equals(executor)) {
                events.add(new MessageEvent("Only the administrator can start the game."));
            } else {
                events.add(new MessageEvent("Cannot start: not enough players (4-10)."));
            }
            return events;
        }
        
        game.start();
        events.add(new MessageEvent("The game begins!"));
        
        return events;
    }
}
