package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public class RolePermissionValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(GameCommand cmd, Player actor, GameSession session) {
        if (actor.getRole() == null) {
            return ValidationResult.INVALID("Le joueur n'a aucun rôle assigné");
        }
        if (!actor.getRole().canPerform(cmd.getActionType())) {
            return ValidationResult.INVALID(
                "L'action " + cmd.getActionType() + " n'est pas autorisée pour le rôle " + actor.getRole().getName()
            );
        }
        return callNext(cmd, actor, session);
    }
}