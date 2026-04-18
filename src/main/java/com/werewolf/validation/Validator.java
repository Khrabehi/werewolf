package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public interface Validator {
    ValidationResult validate(GameCommand command, Player actor, GameSession session);
    Validator setNext(Validator next);
}