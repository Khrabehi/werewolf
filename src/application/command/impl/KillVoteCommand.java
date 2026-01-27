package application.command.impl;

import application.command.GameCommand;
import application.event.GameEvent;
import application.event.impl.MessageEvent;
import domain.model.Game;
import domain.model.Player;
import domain.role.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Commande pour voter pour tuer quelqu'un (loups pendant la nuit)
 */
public class KillVoteCommand implements GameCommand {
    private final String targetPseudo;

    public KillVoteCommand(String targetPseudo) {
        this.targetPseudo = targetPseudo;
    }

    @Override
    public String getName() {
        return "KILL";
    }

    @Override
    public boolean canExecute(Player executor, Game game) {
        if (!executor.isAlive()) {
            return false;
        }
        
        if (executor.getRole() == null || executor.getRole().getTeam() != Team.WEREWOLVES) {
            return false;
        }
        
        if (game.getCurrentPhase() == null || !"NIGHT".equals(game.getCurrentPhase().getName())) {
            return false;
        }
        
        if (executor.hasVoted()) {
            return false;
        }
        
        return true;
    }

    @Override
    public List<GameEvent> execute(Player executor, Game game) {
        List<GameEvent> events = new ArrayList<>();
        
        if (!canExecute(executor, game)) {
            if (!executor.isAlive()) {
                events.add(new MessageEvent("Les joueurs morts ne peuvent pas voter."));
            } else if (executor.getRole() == null || executor.getRole().getTeam() != Team.WEREWOLVES) {
                events.add(new MessageEvent("Seuls les Loups-Garous peuvent tuer."));
            } else if (!"NIGHT".equals(game.getCurrentPhase().getName())) {
                events.add(new MessageEvent("Vous ne pouvez tuer que pendant la nuit."));
            } else if (executor.hasVoted()) {
                events.add(new MessageEvent("Vous avez déjà voté."));
            }
            return events;
        }
        
        // Le vote sera enregistré par le VoteService
        executor.vote();
        events.add(new MessageEvent("Votre vote a été enregistré."));
        
        return events;
    }

    public String getTargetPseudo() {
        return targetPseudo;
    }
}
