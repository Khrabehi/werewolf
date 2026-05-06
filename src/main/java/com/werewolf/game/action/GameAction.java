package com.werewolf.game.action;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;

public interface GameAction {
    void execute(Player actor, Player target, GameSession session);
}