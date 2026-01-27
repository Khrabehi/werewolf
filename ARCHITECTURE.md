# Loup-Garou - Architecture SOLID Refactorée

## 🎯 Principes SOLID Appliqués

### **S - Single Responsibility Principle (SRP)**
Chaque classe a une seule responsabilité :
- `Player` : représente l'état d'un joueur
- `Game` : gère l'état global du jeu
- `GameService` : logique métier du jeu
- `VoteService` : gestion des votes uniquement
- `ClientConnection` : communication réseau uniquement
- `ClientHandler` : pont entre réseau et application

### **O - Open/Closed Principle (OCP)**
Le code est ouvert à l'extension, fermé à la modification :
- **Nouveaux rôles** : créer une classe qui implémente `Role` sans modifier le code existant
- **Nouvelles phases** : implémenter `GamePhase` pour ajouter des phases
- **Nouvelles commandes** : implémenter `GameCommand` pour de nouvelles actions
- **Pattern utilisé** : Strategy Pattern pour les rôles et State Pattern pour les phases

### **L - Liskov Substitution Principle (LSP)**
Les sous-classes peuvent remplacer leurs classes parentes :
- `WerewolfRole` et `VillagerRole` peuvent être utilisés partout où `Role` est attendu
- `NightPhase`, `DayPhase`, `EndPhase` sont interchangeables via `GamePhase`

### **I - Interface Segregation Principle (ISP)**
Interfaces spécifiques et cohésives :
- `Role` : comportement des rôles
- `GamePhase` : cycle de vie des phases
- `GameCommand` : actions utilisateur

### **D - Dependency Inversion Principle (DIP)**
Les modules de haut niveau ne dépendent pas des modules de bas niveau :
- Les services dépendent d'abstractions (`Role`, `GamePhase`, `GameCommand`)
- `RoleFactory` permet l'injection de dépendances
- Pas de couplage direct entre la couche réseau et la logique métier

## 📦 Architecture en Couches (Clean Architecture)

```
src/
├── domain/                    # Couche Domaine (logique métier pure)
│   ├── model/                 # Entités du domaine
│   │   ├── Player.java        # Entité Joueur
│   │   ├── Game.java          # Agrégat principal
│   │   ├── VoteSession.java   # Value Object pour les votes
│   │   └── GameConfiguration.java
│   ├── role/                  # Rôles (Strategy Pattern)
│   │   ├── Role.java          # Interface
│   │   ├── AbstractRole.java  # Classe de base
│   │   ├── Team.java          # Enum des camps
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
├── application/               # Couche Application (cas d'utilisation)
│   ├── service/               # Services métier
│   │   ├── GameService.java  # Orchestration du jeu
│   │   ├── VoteService.java  # Gestion des votes
│   │   └── CommandService.java
│   ├── command/               # Commandes (Command Pattern)
│   │   ├── GameCommand.java  # Interface
│   │   ├── CommandParser.java
│   │   └── impl/
│   │       ├── StartGameCommand.java
│   │       ├── KillVoteCommand.java
│   │       └── SetPseudoCommand.java
│   └── event/                 # Événements (Event-Driven)
│       ├── GameEvent.java     # Classe de base
│       └── impl/
│           ├── PhaseStartEvent.java
│           ├── MessageEvent.java
│           ├── GameEndEvent.java
│           ├── PlayerDeathEvent.java
│           └── RoleAssignedEvent.java
│
├── infrastructure/            # Couche Infrastructure (détails techniques)
│   ├── network/
│   │   ├── ClientConnection.java    # Gestion socket
│   │   ├── ClientHandler.java       # Thread client
│   │   └── GameEventNotifier.java   # Observer Pattern
│   └── server/
│       └── GameServer.java          # Point d'entrée serveur
│
└── presentation/              # Couche Présentation (interface utilisateur)
    └── client/
        └── GameClient.java    # Client console
```

## 🔄 Flux de Données

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

## 🎮 Patterns de Conception Utilisés

1. **Strategy Pattern** : Rôles interchangeables (`Role`, `WerewolfRole`, `VillagerRole`)
2. **State Pattern** : Phases du jeu (`GamePhase`, `NightPhase`, `DayPhase`)
3. **Command Pattern** : Actions utilisateur (`GameCommand`, `StartGameCommand`, etc.)
4. **Factory Pattern** : Création de rôles (`RoleFactory`)
5. **Observer Pattern** : Notification d'événements (`GameEventNotifier`)
6. **Dependency Injection** : Services injectés dans les handlers

## 🚀 Comment Étendre le Jeu

### Ajouter un Nouveau Rôle (ex: Voyante)

```java
// 1. Créer la classe du rôle
public class SeerRole extends AbstractRole {
    public SeerRole() {
        super("Voyante", Team.VILLAGERS, "Vous voyez le rôle d'un joueur chaque nuit");
    }

    @Override
    public boolean canActDuringPhase(String phaseName) {
        return "NIGHT".equalsIgnoreCase(phaseName);
    }

    @Override
    protected void executeAction(Player actor, Player target, Game game) {
        // Logique pour voir le rôle
    }
}

// 2. Enregistrer dans la factory
RoleFactory.registerRole("SEER", new SeerRole());
```

### Ajouter une Nouvelle Commande (ex: VOTE pour le jour)

```java
// 1. Créer la commande
public class DayVoteCommand implements GameCommand {
    private final String targetPseudo;

    public DayVoteCommand(String targetPseudo) {
        this.targetPseudo = targetPseudo;
    }

    @Override
    public String getName() {
        return "VOTE";
    }

    @Override
    public boolean canExecute(Player executor, Game game) {
        return executor.isAlive() && 
               "DAY".equals(game.getCurrentPhase().getName());
    }

    @Override
    public List<GameEvent> execute(Player executor, Game game) {
        // Logique de vote
    }
}

// 2. Ajouter dans CommandParser
case "VOTE":
    return new DayVoteCommand(parts[1].trim());
```

### Ajouter une Nouvelle Phase (ex: Phase de délibération)

```java
public class DiscussionPhase implements GamePhase {
    @Override
    public String getName() {
        return "DISCUSSION";
    }

    @Override
    public List<GameEvent> start(Game game) {
        // Logique de début de phase
    }

    @Override
    public GamePhase next(Game game) {
        return new DayPhase();
    }
}
```

## 📊 Avantages de Cette Architecture

### ✅ Maintenabilité
- Code organisé en couches logiques
- Responsabilités clairement définies
- Facile à naviguer et comprendre

### ✅ Extensibilité
- Ajout de nouveaux rôles sans modifier le code existant
- Nouvelles commandes facilement intégrables
- Phases personnalisables

### ✅ Testabilité
- Services facilement mockables
- Logique métier isolée du réseau
- Tests unitaires simples à écrire

### ✅ Réutilisabilité
- Composants découplés
- Services indépendants
- Patterns réutilisables

## 🧪 Compilation et Exécution

```bash
# Compiler le projet
cd /home/khalis-rabehi/Documents/git/loup-garou
javac -d bin -sourcepath src src/infrastructure/server/GameServer.java
javac -d bin -sourcepath src src/presentation/client/GameClient.java

# Lancer le serveur
java -cp bin infrastructure.server.GameServer

# Lancer un client (dans un autre terminal)
java -cp bin presentation.client.GameClient
```

## 📝 Améliorations Futures Possibles

1. **Tests Unitaires** : JUnit pour tester les services
2. **Configuration Externe** : Fichier properties pour la config
3. **Persistance** : Sauvegarde des parties avec Repository Pattern
4. **Logging** : SLF4J/Logback pour les logs structurés
5. **Validation** : Bean Validation pour valider les entrées
6. **Interface Graphique** : Découplage permet d'ajouter une GUI facilement
7. **Multi-parties** : Gérer plusieurs parties simultanées

## 🎓 Concepts Appliqués

- **Séparation des préoccupations** (Separation of Concerns)
- **Inversion de contrôle** (IoC)
- **Programmation par contrat** (Design by Contract)
- **Architecture hexagonale** (Ports & Adapters)
- **Event-Driven Architecture**
- **Clean Code** et **DRY** (Don't Repeat Yourself)

---

Cette architecture vous permet maintenant d'évoluer le jeu facilement tout en maintenant un code propre et professionnel ! 🎯
