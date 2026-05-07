# Werewolf - Explication de l'architecture et du fonctionnement

## 1. Vue d'ensemble

`Werewolf` est un jeu multijoueur de type Loup-Garou/Mafia, réalisé en Java 17 avec JavaFX pour l'interface graphique et Maven pour la construction du projet. L'objectif du projet est de permettre à plusieurs clients de se connecter à un serveur central, de rejoindre une partie, puis de jouer en temps réel avec des échanges réseau sécurisés.

L'application est organisée en deux grandes parties :

- **Le serveur** : il héberge la partie, gère les joueurs, les rôles, les phases de jeu et la diffusion des mises à jour.
- **Le client** : il fournit l'interface graphique, envoie les actions du joueur et affiche l'état courant de la partie.

La communication entre les deux repose sur du **mTLS** (mutual TLS), donc chaque côté possède son certificat et vérifie celui de l'autre. Les messages de jeu sont ensuite échangés sous forme d'objets Java sérialisés.

## 2. Architecture générale

Le projet suit une architecture en couches assez claire :

- **Couche présentation** : JavaFX, écrans, contrôleurs, vues.
- **Couche modèle client** : stockage local de l'état affiché à l'utilisateur.
- **Couche réseau** : connexion TLS, envoi/réception de messages, gestion des commandes.
- **Couche métier** : logique de jeu, gestion des phases, règles, validation.
- **Couche sécurité** : génération et chargement des certificats, création des contextes SSL.

## 3. Organisation du code

Les packages principaux sont :

- `com.werewolf.client.view` : vues JavaFX, notamment le menu principal et la vue de jeu.
- `com.werewolf.client.controller` : contrôleurs de l'interface.
- `com.werewolf.client.model` : état côté client, noms des joueurs, phase actuelle, rôles, journal d'événements.
- `com.werewolf.client.network` : gestion de la connexion, envoi des actions, réception des mises à jour.
- `com.werewolf.network.server` : serveur principal et gestion par client.
- `com.werewolf.network.shared` : classes de messages partagées entre client et serveur.
- `com.werewolf.game` : logique métier du jeu.
- `com.werewolf.game.action` et `com.werewolf.game.role` : actions possibles et rôles du jeu.
- `com.werewolf.validation` : validation des commandes et des règles.
- `com.werewolf.security` : gestion des certificats et du SSL/TLS.
- `com.werewolf.event` : événements envoyés au client pour mettre à jour l'interface.

## 4. Démarrage de l'application

Le projet est configuré pour JavaFX dans `pom.xml`, et le module Java est défini dans `module-info.java`.

### Côté client

Le point d'entrée utilisateur est la vue JavaFX principale `MainMenuView`.

Le déroulé est le suivant :

1. L'utilisateur ouvre le client.
2. Il saisit un nom d'utilisateur, l'adresse du serveur et le port.
3. `ConnectionManager` établit une connexion TLS vers le serveur.
4. Le client envoie une requête de type `JOIN_GAME`.
5. Si l'adhésion est acceptée, le client bascule vers la vue de jeu.
6. Les mises à jour de partie arrivent ensuite en continu via les messages réseau.

### Côté serveur

Le serveur démarre avec `GameServer`.

Le déroulé est le suivant :

1. Le serveur vérifie ou initialise les certificats mTLS.
2. Il crée un `SSLContext` serveur.
3. Il ouvre un `SSLServerSocket` sur le port configuré.
4. Il attend les connexions des clients.
5. Chaque client est traité par un `ClientHandler` dédié.
6. `GameManager` pilote la partie et diffuse les mises à jour.

## 5. Sécurité réseau

Le projet ne se contente pas d'une connexion TLS classique : il utilise du **mTLS**.

### Pourquoi c'est important

- Le serveur authentifie le client.
- Le client authentifie le serveur.
- On évite les connexions anonymes ou non fiables.
- C'est cohérent avec un jeu réseau distribué où il faut contrôler les échanges.

### Mise en place technique

La classe `CertificateManager` :

- génère la CA locale si nécessaire,
- crée les certificats serveur et client,
- construit les truststores,
- charge les keystores PKCS12.

La classe `SSLContextFactory` :

- construit un `SSLContext` serveur,
- construit un `SSLContext` client,
- branche les `KeyManager` et `TrustManager`.

Ensuite, `GameServer` et `ConnectionManager` utilisent ces contextes pour créer leurs sockets sécurisés.

## 6. Fonctionnement des échanges réseau

Les messages circulent sous forme d'objets Java sérialisés.

### Types de messages

On retrouve notamment :

- `JOIN_GAME` : demande d'entrée dans la partie.
- `START_GAME` : lancement d'une partie.
- `PLAYER_LIST_UPDATE` : liste des joueurs connectés.
- `GAME_STARTED` : notification que la partie a commencé.
- `GAME_STATE_UPDATE` : mise à jour d'état de partie.
- `ERROR` : retour d'erreur côté serveur.
- `PING` ou commandes de jeu : actions spécifiques envoyées au serveur.

### Logique côté client

`ConnectionManager` :

- ouvre la socket SSL,
- envoie la requête de connexion,
- attend la réponse initiale du serveur,
- démarre un thread de lecture pour les messages continus,
- bufferise certaines mises à jour si la vue de jeu n'est pas encore prête.

### Logique côté serveur

`ClientHandler` :

- lit les messages entrants,
- traite les demandes de connexion,
- relaie les commandes de jeu,
- renvoie les mises à jour au client concerné,
- notifie les autres joueurs quand la liste change.

## 7. Logique métier du jeu

La partie elle-même repose sur quelques classes centrales.

### `GameSession`

Elle représente l'état courant de la partie :

- liste des joueurs,
- joueur admin,
- phase de jeu,
- statut des joueurs,
- règles de progression.

### `GameManager`

C'est le pilote principal de la partie.

Il s'occupe de :

- démarrer la partie,
- attribuer les rôles,
- faire avancer les phases,
- déclencher les actions de nuit et de jour,
- détecter la fin de partie,
- diffuser les événements vers les clients.

### `GameState`

`GameState` est l'énumération unique utilisée pour représenter les phases du jeu :

- `LOBBY`
- `NIGHT`
- `DAY_DISCUSSION`
- `DAY_VOTING`
- `GAME_OVER`

Elle sert à synchroniser la logique serveur et l'affichage client.

## 8. Côté interface graphique

L'interface est construite avec JavaFX.

### `MainMenuView`

C'est l'écran initial.

Il permet :

- de saisir l'adresse du serveur,
- de saisir le nom du joueur,
- de rejoindre une partie,
- d'afficher les erreurs ou le statut de connexion.

### `GameView`

Une fois la connexion acceptée, la vue de jeu prend le relais.

Elle affiche :

- la phase en cours,
- le rôle du joueur,
- la liste des joueurs vivants,
- le journal d'événements,
- les actions possibles selon le rôle et la phase,
- les messages de fin de partie.

### `GameViewController`

Le contrôleur fait le lien entre les messages reçus et le modèle client.

Il transforme les `GameStateUpdate` reçus du serveur en mises à jour visibles dans l'interface.

### `GameModel`

Le modèle client garde une copie locale de ce que l'utilisateur doit voir :

- son pseudo,
- son rôle,
- la phase courante,
- les joueurs encore en vie,
- les rôles connus,
- les événements affichés,
- les drapeaux d'action.

Le modèle notifie la vue via des `PropertyChangeListener`, ce qui permet une mise à jour réactive de l'interface.

## 9. Cycle de vie d'une partie

### 1. Connexion

Le client se connecte au serveur via mTLS.

### 2. Inscription

Le client envoie son pseudo avec `JOIN_GAME`.

### 3. Lobby

Le serveur maintient la liste des joueurs connectés et peut afficher un admin.

### 4. Lancement

Quand l'administrateur démarre la partie, le serveur annonce `GAME_STARTED`.

### 5. Attribution des rôles

Le serveur assigne les rôles et envoie des `GAME_STATE_UPDATE` ciblés.

### 6. Phases de jeu

La partie enchaîne les phases : nuit, discussion, vote, etc.

### 7. Fin de partie

Lorsque les conditions de victoire sont remplies, `GameManager` passe à `GAME_OVER` et transmet le gagnant ainsi que l'état final.

## 10. Validation et sécurité des actions

Le projet ne se contente pas de recevoir les commandes : il les valide.

`com.werewolf.validation` vérifie par exemple :

- si l'action est autorisée à la phase courante,
- si le rôle du joueur permet l'action,
- si la cible est valide,
- si le joueur peut encore agir.

Cela évite qu'un client envoie une action invalide qui casserait la logique de la partie.

## 11. Fichiers clés

Voici les fichiers les plus importants :

- `src/main/java/com/werewolf/network/server/GameServer.java` : point d'entrée serveur.
- `src/main/java/com/werewolf/network/server/ClientHandler.java` : gestion des clients.
- `src/main/java/com/werewolf/game/GameManager.java` : moteur de la partie.
- `src/main/java/com/werewolf/game/GameSession.java` : état de la session.
- `src/main/java/com/werewolf/client/network/ConnectionManager.java` : réseau côté client.
- `src/main/java/com/werewolf/client/view/MainMenuView.java` : écran d'accueil.
- `src/main/java/com/werewolf/client/view/GameView.java` : écran de jeu.
- `src/main/java/com/werewolf/security/CertificateManager.java` : certificats mTLS.
- `src/main/java/com/werewolf/security/SSLContextFactory.java` : création des contextes SSL.
- `src/main/java/module-info.java` : configuration du module Java.
