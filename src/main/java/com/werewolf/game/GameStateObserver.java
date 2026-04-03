package com.werewolf.game;

public interface GameStateObserver {
    void onGameStateUpdate(GameStateUpdate update);
}