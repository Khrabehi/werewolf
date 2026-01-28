package application.command;

import domain.model.Game;
import domain.model.Player;
import application.event.GameEvent;
import java.util.List;

/**
 * Command Interface - Command Pattern for user actions
 */
public interface GameCommand {
    /**
     * Validates if the command can be executed
     */
    boolean canExecute(Player executor, Game game);

    /**
     * Executes the command and returns the generated events
     */
    List<GameEvent> execute(Player executor, Game game);

    /**
     * Returns the command name
     */
    String getName();
}
