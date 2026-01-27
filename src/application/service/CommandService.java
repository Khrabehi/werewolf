package application.service;

import application.command.CommandParser;
import application.command.GameCommand;
import application.command.impl.KillVoteCommand;
import application.event.GameEvent;
import application.event.impl.MessageEvent;
import domain.model.Game;
import domain.model.Player;
import domain.role.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des commandes - Principe SRP
 * Responsable de l'orchestration des commandes
 */
public class CommandService {
    private final GameService gameService;
    private final VoteService voteService;

    public CommandService(GameService gameService, VoteService voteService) {
        this.gameService = gameService;
        this.voteService = voteService;
    }

    /**
     * Traite une commande utilisateur
     */
    public List<GameEvent> handleCommand(String input, Player executor) {
        List<GameEvent> events = new ArrayList<>();
        
        GameCommand command = CommandParser.parse(input);
        
        if (command == null) {
            events.add(new MessageEvent("Commande inconnue. Commandes disponibles: PSEUDO, START, KILL"));
            return events;
        }
        
        // Exécuter la commande
        events.addAll(command.execute(executor, gameService.getGame()));
        
        // Traitement spécial pour les votes
        if (command instanceof KillVoteCommand) {
            KillVoteCommand killCommand = (KillVoteCommand) command;
            if (command.canExecute(executor, gameService.getGame())) {
                events.addAll(voteService.registerVote(executor, killCommand.getTargetPseudo()));
                
                // Vérifier si tous les loups ont voté
                List<Player> werewolves = gameService.getGame().getAlivePlayers().stream()
                        .filter(p -> p.getRole() != null && p.getRole().getTeam() == Team.WEREWOLVES)
                        .toList();
                
                if (voteService.allExpectedPlayersVoted(werewolves)) {
                    events.addAll(voteService.resolveVote("tué par les loups-garous"));
                    events.addAll(gameService.advancePhase());
                }
            }
        }
        
        return events;
    }
}
