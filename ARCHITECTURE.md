# Werewolf

## Layout architecture

```
src/
├── domain/                    # Domain Layer
│   ├── model/                 # Domain entity
│   │   ├── Player.java        # Player entity
│   │   ├── Game.java          # Manage the state of the game
│   │   ├── VoteSession.java   # Value Object for vote
│   │   └── GameConfiguration.java
│   ├── role/                  # Roles (design pattern Strategy)
│   │   ├── Role.java          # Interface
│   │   ├── AbstractRole.java  # Base class
│   │   ├── Team.java          # Enum of different team during a game
│   │   ├── RoleFactory.java   # Factory Pattern
│   │   └── impl/
│   │       ├── WerewolfRole.java
│   │       └── VillagerRole.java
│   └── phase/                 # Phases (State Pattern)
│       ├── GamePhase.java     # Interface
│       └── impl/
│           ├── NightPhase.java
│           ├── DayPhase.java
│           └── EndPhase.java
│
├── application/               # Application layer
│   ├── service/              
│   │   ├── GameService.java  # Game orchestration
│   │   ├── VoteService.java  # Votes manager
│   │   └── CommandService.java
│   ├── command/               # Command used to interact during the game (Command Pattern)
│   │   ├── GameCommand.java  # Interface
│   │   ├── CommandParser.java
│   │   └── impl/
│   │       ├── StartGameCommand.java
│   │       ├── KillVoteCommand.java
│   │       └── SetPseudoCommand.java
│   └── event/                 # (Event-Driven)
│       ├── GameEvent.java     # Base class
│       └── impl/
│           ├── PhaseStartEvent.java
│           ├── MessageEvent.java
│           ├── GameEndEvent.java
│           ├── PlayerDeathEvent.java
│           └── RoleAssignedEvent.java
│
├── infrastructure/            # structure layout (network)
│   ├── network/
│   │   ├── ClientConnection.java    # Socket manager
│   │   ├── ClientHandler.java       # Client thread
│   │   └── GameEventNotifier.java   # Observer Pattern
│   └── server/
│       └── GameServer.java          # endpoint of the server
│
└── presentation/              
    └── client/
        └── GameClient.java    # Client console
```

## Data Flow

```
[Client] → [ClientConnection] → [ClientHandler] → [CommandService]
                                                         ↓
                                                   [GameService]
                                                   [VoteService]
                                                         ↓
                                                   [GameEvents]
                                                         ↓
                                 [GameEventNotifier] → [Tous les clients]
```