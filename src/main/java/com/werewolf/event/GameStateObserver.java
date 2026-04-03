package com.werewolf.event;

public interface GameStateObserver {
    void onGameStateUpdate(GameStateUpdate update);
}