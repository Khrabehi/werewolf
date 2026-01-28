package application.event.impl;

import application.event.GameEvent;

/**
 * Game end event
 */
public class GameEndEvent extends GameEvent {
    private final String winner;

    public GameEndEvent(String winner) {
        super("GAME_END");
        this.winner = winner;
    }

    public String getWinner() {
        return winner;
    }

    @Override
    public String getMessage() {
        return "GAME_END " + winner;
    }
}
