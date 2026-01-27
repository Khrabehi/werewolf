package application.command;

import domain.model.Game;
import domain.model.Player;
import application.event.GameEvent;
import java.util.List;

/**
 * Interface Command - Command Pattern pour les actions utilisateur
 * Principe OCP: on peut ajouter de nouvelles commandes sans modifier le code existant
 */
public interface GameCommand {
    /**
     * Valide si la commande peut être exécutée
     */
    boolean canExecute(Player executor, Game game);

    /**
     * Exécute la commande et retourne les événements générés
     */
    List<GameEvent> execute(Player executor, Game game);

    /**
     * Retourne le nom de la commande
     */
    String getName();
}
