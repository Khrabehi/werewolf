package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public class TargetValidationValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(GameCommand cmd, Player actor, GameSession session) {
        String targetId = cmd.getTargetPlayerId();
        
        // Toutes les commandes supportées nécessitent une cible.
        if (targetId == null) {
            return ValidationResult.INVALID("Aucune cible spécifiée");
        }
        
        Player target = session.getPlayer(targetId);
        if (target == null || !target.isAlive()) {
            return ValidationResult.INVALID("Le joueur ciblé est introuvable ou mort");
        }
        
        // Ne pas pouvoir se cibler soi-même (Optionnel, mais pertinent pour KILL et VOTE)
        if (actor.getId().equals(targetId)) {
            // Seul le Médecin (Medic) peut se cibler lui-même (pour se protéger pendant la nuit)
            boolean isMedicSelfHeal = actor.getRole() != null && 
                                      "Medic".equals(actor.getRole().getName()) &&
                                      session.getCurrentPhase() == com.werewolf.game.GameState.NIGHT;
            if (!isMedicSelfHeal) {
                return ValidationResult.INVALID("Vous ne pouvez pas vous cibler vous-même");
            }
        }
        
        return callNext(cmd, actor, session);
    }
}