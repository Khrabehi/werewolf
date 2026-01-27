package domain.role;

import domain.model.Player;
import domain.model.Game;

/**
 * Interface Role - Principe OCP (Open/Closed) et ISP (Interface Segregation)
 * Permet d'ajouter de nouveaux rôles sans modifier le code existant
 */
public interface Role {
    /**
     * Retourne le nom du rôle
     */
    String getName();

    /**
     * Retourne le camp du rôle (WEREWOLVES ou VILLAGERS)
     */
    Team getTeam();

    /**
     * Détermine si ce rôle peut effectuer une action pendant cette phase
     */
    boolean canActDuringPhase(String phaseName);

    /**
     * Exécute l'action du rôle
     */
    void performAction(Player actor, Player target, Game game);

    /**
     * Retourne la description du rôle pour le joueur
     */
    String getDescription();
}
