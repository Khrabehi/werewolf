package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public class PlayerExistenceValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(GameCommand cmd, Player actor, GameSession session) {
        if (actor == null) {
            return ValidationResult.INVALID("Player not found");
        }
        return callNext(cmd, actor, session);
    }
}