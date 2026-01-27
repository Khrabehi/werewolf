package application.event;

/**
 * Classe de base pour les événements du jeu - Principe OCP
 */
public abstract class GameEvent {
    private final String type;
    private final long timestamp;

    protected GameEvent(String type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Retourne le message à broadcaster aux joueurs
     */
    public abstract String getMessage();
}
