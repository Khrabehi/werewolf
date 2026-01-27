package domain.role;

import domain.model.Player;
import domain.model.Game;

/**
 * Classe abstraite pour les rôles - Principe DRY et Template Method Pattern
 */
public abstract class AbstractRole implements Role {
    protected final String name;
    protected final Team team;
    protected final String description;

    protected AbstractRole(String name, Team team, String description) {
        this.name = name;
        this.team = team;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Team getTeam() {
        return team;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void performAction(Player actor, Player target, Game game) {
        validateAction(actor, target, game);
        executeAction(actor, target, game);
    }

    /**
     * Valide si l'action peut être effectuée
     */
    protected void validateAction(Player actor, Player target, Game game) {
        if (!actor.isAlive()) {
            throw new IllegalStateException("Dead players cannot perform actions");
        }
        if (target != null && !target.isAlive()) {
            throw new IllegalArgumentException("Cannot target dead players");
        }
    }

    /**
     * Exécute l'action spécifique au rôle
     */
    protected abstract void executeAction(Player actor, Player target, Game game);

    @Override
    public String toString() {
        return name;
    }
}
