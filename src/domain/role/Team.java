package domain.role;

/**
 * Enum representing possible teams
 */
public enum Team {
    WEREWOLVES("Werewolves"),
    VILLAGERS("Villagers");

    private final String displayName;

    Team(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
