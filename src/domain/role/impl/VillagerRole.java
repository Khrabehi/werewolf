package domain.role.impl;

import domain.model.Game;
import domain.model.Player;
import domain.role.AbstractRole;
import domain.role.Team;

/**
 * Implémentation du rôle Villageois
 */
public class VillagerRole extends AbstractRole {

    public VillagerRole() {
        super("Villageois", Team.VILLAGERS,
              "Vous êtes un simple Villageois. Trouvez et éliminez les Loups-Garous.");
    }

    @Override
    public boolean canActDuringPhase(String phaseName) {
        return "DAY".equalsIgnoreCase(phaseName);
    }

    @Override
    protected void executeAction(Player actor, Player target, Game game) {
        // Les villageois votent pendant le jour (géré par VoteService)
    }
}
