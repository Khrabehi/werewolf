package application.service;

import application.command.CommandParser;
import application.command.GameCommand;
import application.command.impl.KillVoteCommand;
import application.command.impl.StartGameCommand;
import application.event.GameEvent;
import application.event.impl.MessageEvent;
import domain.model.Game;
import domain.model.Player;
import domain.role.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Command management service - SRP Principle
 * Responsible for command orchestration
 */
public class CommandService {
    private final GameService gameService;
    private final VoteService voteService;

    public CommandService(GameService gameService, VoteService voteService) {
        this.gameService = gameService;
        this.voteService = voteService;
    }

    /**
     * Processes a user command
     */
    public List<GameEvent> handleCommand(String input, Player executor) {
        List<GameEvent> events = new ArrayList<>();
        
        GameCommand command = CommandParser.parse(input);
        
        if (command == null) {
            events.add(new MessageEvent("Unknown command. Available commands: PSEUDO, START, KILL"));
            return events;
        }
        
        // Special handling for START (assign roles + start phase)
        if (command instanceof StartGameCommand) {
            if (command.canExecute(executor, gameService.getGame())) {
                events.addAll(gameService.startGame());
            } else {
                events.addAll(command.execute(executor, gameService.getGame()));
            }
            return events;
        }
        
        // Special handling for KILL (werewolves vote)
        if (command instanceof KillVoteCommand) {
            KillVoteCommand killCommand = (KillVoteCommand) command;
            
            if (!command.canExecute(executor, gameService.getGame())) {
                events.addAll(command.execute(executor, gameService.getGame()));
                return events;
            }
            
            // Register the vote
            events.addAll(voteService.registerVote(executor, killCommand.getTargetPseudo()));
            
            // Check if all werewolves have voted
            List<Player> werewolves = gameService.getGame().getAlivePlayers().stream()
                    .filter(p -> p.getRole() != null && p.getRole().getTeam() == Team.WEREWOLVES)
                    .toList();
            
            if (voteService.allExpectedPlayersVoted(werewolves)) {
                events.add(new MessageEvent("All werewolves have voted!"));
                events.addAll(voteService.resolveVote("killed by werewolves"));
                events.addAll(gameService.advancePhase());
            }
            
            return events;
        }
        
        // Execute other commands normally
        events.addAll(command.execute(executor, gameService.getGame()));
        
        return events;
    }
}
