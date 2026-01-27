# 🐺 Loup-Garou

Un jeu Loup-Garou en réseau développé en Java

## 🏗️ Architecture

```
src/
├── domain/          # Logique métier pure (aucune dépendance)
├── application/     # Cas d'usage (services, commandes, événements)
├── infrastructure/  # Détails techniques (réseau, serveur)
└── presentation/    # Interface utilisateur (client console)
```

Voir [ARCHITECTURE.md](ARCHITECTURE.md) pour plus de détails.

## 🚀 Installation et Lancement

### Prérequis
- Java 17+ (JDK, pas seulement JRE)
- `javac` doit être disponible

### Compilation
```bash
./compile.sh
```

### Lancer le serveur
```bash
java -cp bin infrastructure.server.GameServer
```

### Lancer un client (dans un autre terminal)
```bash
java -cp bin presentation.client.GameClient
```

Voir [INSTALL.md](INSTALL.md) pour plus de détails.

## 🎮 Comment Jouer

1. Lancer le serveur
2. Connecter 4-10 clients
3. Chaque client entre son pseudo : `PSEUDO <votre_pseudo>`
4. Le premier joueur connecté (admin) démarre : `START`
5. Les rôles sont distribués automatiquement
6. **Nuit** : Les loups votent avec `KILL <pseudo>`
7. **Jour** : Discussion et élimination
8. Le jeu continue jusqu'à la victoire d'un camp

## 📚 Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - Architecture détaillée
- [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) - Comparaison ancien vs nouveau code
- [DIAGRAMS.md](DIAGRAMS.md) - Diagrammes et visualisations
- [INSTALL.md](INSTALL.md) - Installation et compilation
- [SUMMARY.md](SUMMARY.md) - Résumé complet du refactoring

## 🎨 Patterns de Conception

1. **Strategy Pattern** - Rôles interchangeables
2. **State Pattern** - Gestion des phases du jeu
3. **Command Pattern** - Actions utilisateur découplées
4. **Factory Pattern** - Création de rôles
5. **Observer Pattern** - Notifications d'événements
6. **Template Method** - Code réutilisable pour les rôles

## 💡 Extensibilité

### Ajouter un nouveau rôle (Voyante)
```java
public class SeerRole extends AbstractRole {
    public SeerRole() {
        super("Voyante", Team.VILLAGERS, "Description");
    }
    
    @Override
    protected void executeAction(Player actor, Player target, Game game) {
        // Logique
    }
}

// Enregistrer
RoleFactory.registerRole("SEER", new SeerRole());
```

**Résultat** : 1 nouveau fichier, 0 modifications ailleurs !

## 🧪 Tests

Des tests unitaires sont inclus dans le dossier `test/` :
- `GameTest.java` - Tests du modèle Game
- `RoleTest.java` - Tests des rôles
- `VoteSessionTest.java` - Tests du système de vote

## Folder Structure

The workspace contains the following folders:

- `src`: the folder to maintain sources (organized by layers)
  - `domain/`: Business logic (no dependencies)
  - `application/`: Use cases (services, commands, events)
  - `infrastructure/`: Technical details (network, server)
  - `presentation/`: User interface (console client)
- `lib`: the folder to maintain dependencies
- `bin`: compiled output files
- `test`: unit tests

## 📝 Licence

Voir [LICENSE](LICENSE) pour plus de détails.

