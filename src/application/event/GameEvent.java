package application.event;

/**
 * Base class for game events - OCP Principle
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
     * Returns the message to broadcast to players
     */
    public abstract String getMessage();
}
