package domain.role.impl;

import domain.model.Game;
import domain.model.Player;
import domain.role.AbstractRole;
import domain.role.Team;

/**
 * Implémentation du rôle Loup-Garou
 * Principe OCP: nouvelle implémentation sans modifier le code existant
 */
public class WerewolfRole extends AbstractRole {

    public WerewolfRole() {
        super("Loup-Garou", Team.WEREWOLVES, 
              "Vous êtes un Loup-Garou. Tuez les villageois chaque nuit pour gagner.");
    }

    @Override
    public boolean canActDuringPhase(String phaseName) {
        return "NIGHT".equalsIgnoreCase(phaseName);
    }

    @Override
    protected void executeAction(Player actor, Player target, Game game) {
        // L'action de tuer est gérée par le VoteService
        // Ce rôle autorise simplement le vote pendant la nuit
    }
}
