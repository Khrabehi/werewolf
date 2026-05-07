# werewolf
Projet scolaire, l'objectif est de créer un projet qui inclut une communication réseau sécurisée.

## Prerequisites

- Java 17+
- Maven 3.8+

## Lancer le jeu

Le jeu fonctionne en deux étapes : lancer d'abord le serveur, puis ouvrir un ou plusieurs clients.

### 1. Démarrer le serveur

Depuis la racine du projet :

```bash
export GAMESERVER_STORE_PASSWORD=werewolf
mvn -DskipTests -Dexec.mainClass="com.werewolf.network.server.GameServer" \
  -Dexec.classpathScope=runtime \
  org.codehaus.mojo:exec-maven-plugin:3.1.0:java
```

### 2. Démarrer un client

Dans un autre terminal :

```bash
export GAMECLIENT_STORE_PASSWORD=werewolf
mvn clean javafx:run -Djavafx.mainClass=com.werewolf/com.werewolf.client.view.MainMenuView
```

Cette commande ouvre l'interface graphique JavaFX du client.

Le mot de passe des keystores doit être le même côté serveur et côté client. Si vous utilisez une autre valeur, remplacez simplement `werewolf` dans les commandes ci-dessus.

## Run unit tests

Depuis la racine du projet:
```bash
mvn test
```

Cette commande run tous les tests unitaires définis dans le projet.

## Architecture

- Interface Client : Vues JavaFX dans `com.werewolf.client.view` pour le menu principal et l'écran de jeu.
- Logique Client : Contrôleurs et modèles dans `com.werewolf.client.controller` et `com.werewolf.client.model`.
- Réseau : `ConnectionManager` se connecte au serveur et envoie/reçoit des objets `Message` sérialisés.
- Serveur : `GameServer` accepte les clients, `ClientHandler` gère les messages par client.
- Cœur du jeu : `GameSession` contient les joueurs/l'état, `GameManager` pilote la boucle et les phases de jeu.
- Mises à jour : Le serveur diffuse des `GAME_STATE_UPDATE` aux clients pour mettre à jour l'interface.
