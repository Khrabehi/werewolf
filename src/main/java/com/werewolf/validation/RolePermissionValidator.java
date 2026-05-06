package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public class RolePermissionValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(GameCommand cmd, Player actor, GameSession session) {
        if (actor.getRole() == null) {
            return ValidationResult.INVALID("Player has no role assigned");
        }
        if (!actor.getRole().canPerform(cmd.getActionType())) {
            return ValidationResult.INVALID(
                "Action " + cmd.getActionType() + " not allowed for role " + actor.getRole().getName()
            );
        }
        return callNext(cmd, actor, session);
    }
}