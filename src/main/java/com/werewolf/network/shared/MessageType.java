package com.werewolf.network.shared;

public enum MessageType {
    // Basic types (Infrastructure and Chat)
    PING,
    PONG, 
    CONNECT,
    DISCONNECT, 
    CHAT,
    
    // Player action commands
    KILL,           // Werewolves designate a victim
    VOTE,           // Village votes during the day to eliminate a suspect
    HEAL,        // Doctor/Savior protects a player
    PEEK,    // Seer inspects the role of a player
    
    // Responses and server state management
    ERROR,                 // Rejection of a command (e.g., cheating, out of turn)
    GAME_STATE_UPDATE,     // Server announces a change (day/night, deaths, etc.)
    GAME_COMMAND_RESPONSE  // Confirmation that an action has been taken into account
}