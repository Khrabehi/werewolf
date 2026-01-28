package domain.role;

import domain.model.Player;
import domain.model.Game;

/**
 * Role Interface - OCP (Open/Closed) and ISP (Interface Segregation) Principles
 * Allows adding new roles without modifying existing code
 */
public interface Role {
    /**
     * Returns the role name
     */
    String getName();

    /**
     * Returns the role's team (WEREWOLVES or VILLAGERS)
     */
    Team getTeam();

    /**
     * Determines if this role can perform an action during this phase
     */
    boolean canActDuringPhase(String phaseName);

    /**
     * Executes the role's action
     */
    void performAction(Player actor, Player target, Game game);

    /**
     * Returns the role description for the player
     */
    String getDescription();
}
