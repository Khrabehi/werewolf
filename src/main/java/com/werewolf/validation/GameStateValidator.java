package com.werewolf.validation;

import com.werewolf.game.GameState;
import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public class GameStateValidator extends AbstractValidator {
    @Override
    public ValidationResult validate(GameCommand cmd, Player actor, GameSession session) {
        String actionType = cmd.getActionType();

        if ("KILL".equals(actionType) && session.getCurrentPhase() != GameState.NIGHT) {
            return ValidationResult.INVALID("L'action d'éliminer n'est autorisée que la nuit");
        }
        if ("VOTE".equals(actionType) && session.getCurrentPhase() != GameState.DAY_VOTING) {
            return ValidationResult.INVALID("Le vote n'est autorisé que pendant la phase de vote de jour");
        }
        if (("HEAL".equals(actionType) || "PEEK".equals(actionType)) && session.getCurrentPhase() != GameState.NIGHT) {
            return ValidationResult.INVALID("L'action " + actionType + " n'est autorisée que la nuit");
        }

        return callNext(cmd, actor, session);
    }
}