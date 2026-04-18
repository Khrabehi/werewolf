package com.werewolf.validation;

import com.werewolf.game.GamePhase;
import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public class GameStateValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(GameCommand cmd, Player actor, GameSession session) {
        String actionType = cmd.getActionType();

        if ("KILL".equals(actionType) && session.getCurrentPhase() != GamePhase.NIGHT) {
            return ValidationResult.INVALID("Kill action only allowed at night");
        }
        if ("VOTE".equals(actionType) && session.getCurrentPhase() != GamePhase.DAY_VOTING) {
            return ValidationResult.INVALID("Vote action only allowed during day voting phase");
        }
        if (("HEAL".equals(actionType) || "PEEK".equals(actionType)) && session.getCurrentPhase() != GamePhase.NIGHT) {
            return ValidationResult.INVALID(actionType + " action only allowed at night");
        }
        if ("PEEK".equals(actionType) && session.getCurrentPhase() != GamePhase.NIGHT) {
            return ValidationResult.INVALID("Peek action only allowed at night");
        }

        return callNext(cmd, actor, session);
    }
}