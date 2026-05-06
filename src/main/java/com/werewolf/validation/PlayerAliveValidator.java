package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public class PlayerAliveValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(GameCommand cmd, Player actor, GameSession session) {
        if (!actor.isAlive()) {
            return ValidationResult.INVALID("Dead players cannot act");
        }
        return callNext(cmd, actor, session);
    }
}