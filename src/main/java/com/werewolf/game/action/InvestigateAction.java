package com.werewolf.game.action;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;

public class InvestigateAction implements GameAction {
    @Override
    public void execute(Player actor, Player target, GameSession session) {
        System.out.println("Seer " + actor.getUsername() + " investigated " + target.getUsername());
        String roleName = target.getRole().getName();
        
        session.sendPrivateMessage(actor.getId(), "L'enquête révèle que " + target.getUsername() + " est " + roleName);
    }
}