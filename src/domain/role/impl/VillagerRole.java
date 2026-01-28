package domain.role.impl;

import domain.model.Game;
import domain.model.Player;
import domain.role.AbstractRole;
import domain.role.Team;

/**
 * Villager role implementation
 */
public class VillagerRole extends AbstractRole {

    public VillagerRole() {
        super("Villager", Team.VILLAGERS,
              "You are a simple Villager. Find and eliminate the Werewolves.");
    }

    @Override
    public boolean canActDuringPhase(String phaseName) {
        return "DAY".equalsIgnoreCase(phaseName);
    }

    @Override
    protected void executeAction(Player actor, Player target, Game game) {
        // Villagers vote during the day (managed by VoteService)
    }
}
