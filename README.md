# Werewolf

A networked Werewolf game developed in Java

## Architecture

```
src/
├── domain/          # Pure business logic (no dependencies)
├── application/     # Use cases (services, commands, events)
├── infrastructure/  # Technical details (network, server)
└── presentation/    # User interface (console client)
```

Check [ARCHITECTURE.md](ARCHITECTURE.md) for more details. 

## Installation and Launch

### Prerequisites
- Java 17+ (JDK, not just JRE)
- `javac` must be available

### Compilation
```bash
./compile.sh
```

### Launch the server
```bash
java -cp bin infrastructure.server.GameServer
```

### Launch a client (in another terminal)
```bash
java -cp bin presentation.client.GameClient
```

See [INSTALL.md](INSTALL.md) for more details.

## How to Play

1. Launch the server
2. Connect 4-10 clients
3. Each client enters their username: `PSEUDO <your_username>`
4. The first connected player (admin) starts: `START`
5. Roles are automatically distributed
6. **Night**: Werewolves vote with `KILL <username>`
7. **Day**: Discussion and elimination
8. The game continues until one team wins

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed architecture
- [INSTALL.md](INSTALL.md) - Installation and compilation

## Tests

Unit tests are included in the `test/` folder:
- `GameTest.java` - Game model tests
- `RoleTest.java` - Role tests
- `VoteSessionTest.java` - Voting system tests

## License

See [LICENSE](LICENSE) for more details.

