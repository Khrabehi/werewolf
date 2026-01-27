package application.command.impl;

import application.command.GameCommand;
import application.event.GameEvent;
import application.event.impl.MessageEvent;
import domain.model.Game;
import domain.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Commande pour définir le pseudo
 */
public class SetPseudoCommand implements GameCommand {
    private final String pseudo;

    public SetPseudoCommand(String pseudo) {
        this.pseudo = pseudo;
    }

    @Override
    public String getName() {
        return "PSEUDO";
    }

    @Override
    public boolean canExecute(Player executor, Game game) {
        return !game.isStarted() && pseudo != null && !pseudo.trim().isEmpty();
    }

    @Override
    public List<GameEvent> execute(Player executor, Game game) {
        List<GameEvent> events = new ArrayList<>();
        
        if (!canExecute(executor, game)) {
            if (game.isStarted()) {
                events.add(new MessageEvent("Impossible de changer de pseudo : la partie a déjà commencé."));
            } else {
                events.add(new MessageEvent("Pseudo invalide."));
            }
            return events;
        }
        
        executor.setPseudo(pseudo);
        events.add(new MessageEvent(pseudo + " a rejoint la partie !"));
        
        return events;
    }

    public String getPseudo() {
        return pseudo;
    }
}
