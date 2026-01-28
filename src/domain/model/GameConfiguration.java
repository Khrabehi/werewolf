package domain.model;

/**
 * Value Object - Game configuration
 */
public class GameConfiguration {
    private final int minPlayers;
    private final int maxPlayers;
    private final int werewolvesRatio; // 1 werewolf per X players

    public GameConfiguration(int minPlayers, int maxPlayers, int werewolvesRatio) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.werewolvesRatio = werewolvesRatio;
    }

    public static GameConfiguration defaultConfiguration() {
        return new GameConfiguration(4, 10, 4);
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int calculateWerewolvesCount(int totalPlayers) {
        return Math.max(1, totalPlayers / werewolvesRatio);
    }
}
