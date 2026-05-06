package com.werewolf.game.action;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;

public class ProtectAction implements GameAction {
    @Override
    public void execute(Player actor, Player target, GameSession session) {
        System.out.println("Medic " + actor.getUsername() + " protected " + target.getUsername());
        target.setProtected(true);
    }
}
