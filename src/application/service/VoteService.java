package application.service;

import domain.model.Game;
import domain.model.Player;
import domain.model.VoteSession;
import application.event.GameEvent;
import application.event.impl.MessageEvent;
import application.event.impl.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Vote management service - SRP Principle
 * Responsible only for voting logic
 */
public class VoteService {
    private final VoteSession currentVoteSession;
    private final Game game;

    public VoteService(Game game) {
        this.game = game;
        this.currentVoteSession = new VoteSession();
    }

    /**
     * Registers a vote
     */
    public List<GameEvent> registerVote(Player voter, String targetPseudo) {
        List<GameEvent> events = new ArrayList<>();
        
        try {
            // Check that the target exists and is alive
            Optional<Player> targetOpt = game.findPlayerByPseudo(targetPseudo);
            if (targetOpt.isEmpty() || !targetOpt.get().isAlive()) {
                events.add(new MessageEvent("Invalid target for the vote."));
                return events;
            }
            
            // Register the vote
            currentVoteSession.registerVote(voter.getId(), targetPseudo);
            events.add(new MessageEvent("Vote registered."));
            
        } catch (IllegalStateException e) {
            events.add(new MessageEvent(e.getMessage()));
        }
        
        return events;
    }

    /**
     * Checks if all expected players have voted
     */
    public boolean allExpectedPlayersVoted(List<Player> expectedVoters) {
        return expectedVoters.stream()
                .filter(Player::isAlive)
                .allMatch(p -> currentVoteSession.hasVoted(p.getId()));
    }

    /**
     * Resolves the vote and returns the events
     */
    public List<GameEvent> resolveVote(String reason) {
        List<GameEvent> events = new ArrayList<>();
        
        Optional<String> winnerOpt = currentVoteSession.getWinner();
        
        if (winnerOpt.isEmpty()) {
            events.add(new MessageEvent("No votes recorded."));
        } else {
            String victimPseudo = winnerOpt.get();
            Optional<Player> victimOpt = game.findPlayerByPseudo(victimPseudo);
            
            if (victimOpt.isPresent()) {
                Player victim = victimOpt.get();
                victim.kill();
                events.add(new PlayerDeathEvent(victimPseudo, reason));
            }
        }
        
        // Reset the vote session
        currentVoteSession.clear();
        
        return events;
    }

    /**
     * Cancels the current vote session
     */
    public void clearVotes() {
        currentVoteSession.clear();
    }

    public VoteSession getCurrentVoteSession() {
        return currentVoteSession;
    }
}
