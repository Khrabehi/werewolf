package application.event.impl;

import application.event.GameEvent;

/**
 * Phase start event
 */
public class PhaseStartEvent extends GameEvent {
    private final String phaseName;

    public PhaseStartEvent(String phaseName) {
        super("PHASE_START");
        this.phaseName = phaseName;
    }

    public String getPhaseName() {
        return phaseName;
    }

    @Override
    public String getMessage() {
        return "PHASE " + phaseName;
    }
}
