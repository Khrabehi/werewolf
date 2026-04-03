package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public class TargetValidationValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(GameCommand cmd, Player actor, GameSession session) {
        String targetId = cmd.getTargetPlayerId();
        
        // Certaines commandes n'ont pas de cible (ex: passer son tour)
        if (targetId == null) {
            return ValidationResult.INVALID("No target specified");
        }
        
        Player target = session.getPlayer(targetId);
        if (target == null || !target.isAlive()) {
            return ValidationResult.INVALID("Target player not found or is dead");
        }
        
        // Ne pas pouvoir se cibler soi-même (Optionnel, mais pertinent pour KILL et VOTE)
        if (actor.getId().equals(targetId)) {
            return ValidationResult.INVALID("Cannot target yourself");
        }
        
        return callNext(cmd, actor, session);
    }
}