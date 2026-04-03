package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.network.shared.GameCommand;

public abstract class AbstractValidator implements Validator {
    protected Validator next;
    
    @Override
    public Validator setNext(Validator next) {
        this.next = next;
        return next; 
    }
    
    protected ValidationResult callNext(GameCommand cmd, Player actor, GameSession session) {
        if (next != null) {
            return next.validate(cmd, actor, session);
        }
        return ValidationResult.VALID();
    }
}