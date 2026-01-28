package application.event.impl;

import application.event.GameEvent;

/**
 * General message event
 */
public class MessageEvent extends GameEvent {
    private final String message;

    public MessageEvent(String message) {
        super("MESSAGE");
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "MESSAGE " + message;
    }
}
