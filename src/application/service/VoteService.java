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
 * Service de gestion des votes - Principe SRP
 * Responsable uniquement de la logique de vote
 */
public class VoteService {
    private final VoteSession currentVoteSession;
    private final Game game;

    public VoteService(Game game) {
        this.game = game;
        this.currentVoteSession = new VoteSession();
    }

    /**
     * Enregistre un vote
     */
    public List<GameEvent> registerVote(Player voter, String targetPseudo) {
        List<GameEvent> events = new ArrayList<>();
        
        try {
            // Vérifier que la cible existe et est vivante
            Optional<Player> targetOpt = game.findPlayerByPseudo(targetPseudo);
            if (targetOpt.isEmpty() || !targetOpt.get().isAlive()) {
                events.add(new MessageEvent("Cible invalide pour le vote."));
                return events;
            }
            
            // Enregistrer le vote
            currentVoteSession.registerVote(voter.getId(), targetPseudo);
            events.add(new MessageEvent("Vote enregistré."));
            
        } catch (IllegalStateException e) {
            events.add(new MessageEvent(e.getMessage()));
        }
        
        return events;
    }

    /**
     * Vérifie si tous les joueurs attendus ont voté
     */
    public boolean allExpectedPlayersVoted(List<Player> expectedVoters) {
        return expectedVoters.stream()
                .filter(Player::isAlive)
                .allMatch(p -> currentVoteSession.hasVoted(p.getId()));
    }

    /**
     * Résout le vote et retourne les événements
     */
    public List<GameEvent> resolveVote(String reason) {
        List<GameEvent> events = new ArrayList<>();
        
        Optional<String> winnerOpt = currentVoteSession.getWinner();
        
        if (winnerOpt.isEmpty()) {
            events.add(new MessageEvent("Aucun vote enregistré."));
        } else {
            String victimPseudo = winnerOpt.get();
            Optional<Player> victimOpt = game.findPlayerByPseudo(victimPseudo);
            
            if (victimOpt.isPresent()) {
                Player victim = victimOpt.get();
                victim.kill();
                events.add(new PlayerDeathEvent(victimPseudo, reason));
            }
        }
        
        // Réinitialiser la session de vote
        currentVoteSession.clear();
        
        return events;
    }

    /**
     * Annule la session de vote actuelle
     */
    public void clearVotes() {
        currentVoteSession.clear();
    }

    public VoteSession getCurrentVoteSession() {
        return currentVoteSession;
    }
}
