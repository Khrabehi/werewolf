package domain.phase;

import domain.model.Game;
import application.event.GameEvent;
import java.util.List;

/**
 * Interface GamePhase - Principe OCP et State Pattern
 * Permet d'ajouter de nouvelles phases sans modifier le code existant
 */
public interface GamePhase {
    /**
     * Retourne le nom de la phase
     */
    String getName();

    /**
     * Démarre la phase
     */
    List<GameEvent> start(Game game);

    /**
     * Termine la phase et retourne la phase suivante
     */
    GamePhase end(Game game);

    /**
     * Vérifie si la phase peut se terminer
     */
    boolean canEnd(Game game);

    /**
     * Retourne la phase suivante
     */
    GamePhase next(Game game);
}
