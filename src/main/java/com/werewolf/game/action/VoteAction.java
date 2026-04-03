package com.werewolf.game.action;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;

public class VoteAction implements GameAction {
    @Override
    public void execute(Player actor, Player target, GameSession session) {
        System.out.println(actor.getUsername() + " voted for " + target.getUsername());
        session.recordVote(actor.getId(), target.getId());
    }
}
