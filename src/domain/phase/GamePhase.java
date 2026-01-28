package domain.phase;

import domain.model.Game;
import application.event.GameEvent;
import java.util.List;

/**
 * GamePhase Interface - OCP Principle and State Pattern
 * Allows adding new phases without modifying existing code
 */
public interface GamePhase {
    /**
     * Returns the phase name
     */
    String getName();

    /**
     * Starts the phase
     */
    List<GameEvent> start(Game game);

    /**
     * Ends the phase and returns the next phase
     */
    GamePhase end(Game game);

    /**
     * Checks if the phase can end
     */
    boolean canEnd(Game game);

    /**
     * Returns the next phase
     */
    GamePhase next(Game game);
}
