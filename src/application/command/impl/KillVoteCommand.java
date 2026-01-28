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
 * Command to vote to kill someone (werewolves during the night)
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
                events.add(new MessageEvent("Dead players cannot vote."));
            } else if (executor.getRole() == null || executor.getRole().getTeam() != Team.WEREWOLVES) {
                events.add(new MessageEvent("Only Werewolves can kill."));
            } else if (!"NIGHT".equals(game.getCurrentPhase().getName())) {
                events.add(new MessageEvent("You can only kill during the night."));
            } else if (executor.hasVoted()) {
                events.add(new MessageEvent("You have already voted."));
            }
            return events;
        }
        
        // The vote will be registered by the VoteService
        executor.vote();
        events.add(new MessageEvent("Your vote has been registered."));
        
        return events;
    }

    public String getTargetPseudo() {
        return targetPseudo;
    }
}
