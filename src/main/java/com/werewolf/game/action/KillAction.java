package com.werewolf.game.action;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;

public class KillAction implements GameAction {
    @Override
    public void execute(Player actor, Player target, GameSession session) {
        System.out.println("Werewolf " + actor.getUsername() + " killed " + target.getUsername());
        
        if (!target.isProtected()) {
            target.setAlive(false);
            session.notifySessionUpdate("Player " + target.getUsername() + " was killed by werewolves.");
        } else {
            System.out.println("Kill failed: " + target.getUsername() + " was protected.");
        }
    }
}