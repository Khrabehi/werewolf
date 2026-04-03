package com.werewolf.game.action;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;

public class KillAction implements GameAction {
    @Override
    public void execute(Player actor, Player target, GameSession session) {
        if (!target.isProtected()) {
            target.setAlive(false);
            System.out.println("Nuit : " + target.getUsername() + " a été dévoré par les loups.");
        } else {
            System.out.println("Nuit : " + target.getUsername() + " a été attaqué mais le médecin l'a sauvé !");
        }
    }
}