# werewolf
School project, the goal is to create a project that included secure network communication. 

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

### 3. Démarrer plusieurs clients

Pour ouvrir 4 clients en même temps :

```bash
bash scripts/run_clients.sh 4 werewolf
```

Le mot de passe des keystores doit être le même côté serveur et côté client. Si vous utilisez une autre valeur, remplacez simplement `werewolf` dans les commandes ci-dessus.

## Run unit tests

From the project root:

```bash
mvn test
```

This command runs all unit tests in `src/test/java`.

## Architecture

- Client UI: JavaFX views in `com.werewolf.client.view` for the main menu and in-game screen.
- Client logic: Controllers and models in `com.werewolf.client.controller` and `com.werewolf.client.model`.
- Network: `ConnectionManager` connects to the server and sends/receives serialized `Message` objects.
- Server: `GameServer` accepts clients, `ClientHandler` handles per-client messages.
- Game core: `GameSession` holds players/state, `GameManager` drives the game loop and phases.
- Updates: Server broadcasts `GAME_STATE_UPDATE` to clients to drive UI changes.
