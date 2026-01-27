package application.command.impl;

import application.command.GameCommand;
import application.event.GameEvent;
import application.event.impl.MessageEvent;
import domain.model.Game;
import domain.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Commande pour démarrer le jeu - Command Pattern
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
                events.add(new MessageEvent("Seul l'administrateur peut démarrer la partie."));
            } else {
                events.add(new MessageEvent("Impossible de démarrer : pas assez de joueurs (4-10)."));
            }
            return events;
        }
        
        game.start();
        events.add(new MessageEvent("La partie commence !"));
        
        return events;
    }
}
