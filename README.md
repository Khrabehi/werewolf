# werewolf
School project, the goal is to create a project that included secure network communication. 

## Prerequisites

- Java 17+
- Maven 3.8+

## Run the project

From the project root:

```bash
export GAMECLIENT_STORE_PASSWORD=change_me_123456
mvn clean javafx:run -Djavafx.mainClass=com.werewolf/com.werewolf.client.view.MainMenuView
```

This command compiles and starts the JavaFX client UI.

## Run the server

From the project root:

```bash
export GAMESERVER_STORE_PASSWORD=change_me_123456
mvn -DskipTests -Dexec.mainClass="com.werewolf.network.server.GameServer" \
	-Dexec.classpathScope=runtime \
	org.codehaus.mojo:exec-maven-plugin:3.1.0:java
```

Use the same password value for client and server. The password must be at least 6 characters.

## Run unit tests

From the project root:

```bash
mvn test
```

This command runs all unit tests in `src/test/java`.
