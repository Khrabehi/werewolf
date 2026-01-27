package domain.role;

/**
 * Enum représentant les camps possibles
 */
public enum Team {
    WEREWOLVES("Loups-Garous"),
    VILLAGERS("Villageois");

    private final String displayName;

    Team(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
