package domain.role.impl;

import domain.model.Game;
import domain.model.Player;
import domain.role.AbstractRole;
import domain.role.Team;

/**
 * Werewolf role implementation
 * OCP Principle: new implementation without modifying existing code
 */
public class WerewolfRole extends AbstractRole {

    public WerewolfRole() {
        super("Werewolf", Team.WEREWOLVES, 
              "You are a Werewolf. Kill the villagers each night to win.");
    }

    @Override
    public boolean canActDuringPhase(String phaseName) {
        return "NIGHT".equalsIgnoreCase(phaseName);
    }

    @Override
    protected void executeAction(Player actor, Player target, Game game) {
        // The kill action is managed by VoteService
        // This role simply allows voting during the night
    }
}
