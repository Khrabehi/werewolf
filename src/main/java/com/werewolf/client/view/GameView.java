package com.werewolf.client.view;

import com.werewolf.client.controller.GameViewController;
import com.werewolf.client.model.GameModel;
import com.werewolf.game.GameState;
import com.werewolf.game.Player;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.beans.PropertyChangeListener;

public class GameView {

    private static final String BG_DEEP    = "#0d0d2b";
    private static final String BG_PANEL   = "#111130";
    private static final String BG_HEADER  = "#1a1a4e";
    private static final String GOLD       = "#e0c97f";
    private static final String MUTED_BLUE = "#aaaacc";
    private static final String BTN_RED    = "#8b0000";
    private static final String BTN_ACTIVE = "#c0392b";

    private final Stage stage;
    private final GameModel model;
    private final GameViewController controller;
    private final Runnable onGameOverClose;

    private boolean gameOverHandled;

    // Scène principale
    private BorderPane root;

    // Panneaux de mise en page pour le thème dynamique
    private HBox headerPane;
    private VBox playerPanel;
    private VBox eventLogPanel;
    private VBox actionPanelBox;

    // En-tête
    private Label phaseLabel;
    private Label roleLabel;
    private Label timerLabel;

    // État du chronomètre
    private Timeline phaseTimer;
    private Timeline returnToLobbyTimer;
    private int timeRemaining;
    private GameState currentPhase;
    private final PropertyChangeListener modelListener = evt -> Platform.runLater(this::refreshUI);

    // Barre latérale des joueurs
    private ListView<String> playerListView;

    // Journal des événements
    private ListView<String> eventLogView;
    private TextField chatField;
    private Button sendBtn;

    // Panneau d'action
    private Label actionPromptLabel;
    private ListView<String> targetListView;
    private Button actionButton;

    public GameView(Stage stage, GameModel model, GameViewController controller, Runnable onGameOverClose) {
        this.stage = stage;
        this.model = model;
        this.controller = controller;
        this.onGameOverClose = onGameOverClose;
        this.gameOverHandled = false;
    }

    public void show() {
        Scene scene = buildScene();
        stage.setScene(scene);
        stage.setWidth(960);
        stage.setHeight(680);
        stage.setResizable(true);
        stage.setTitle("Loup-Garou – En jeu");

        model.addPropertyChangeListener(modelListener);
        refreshUI();
    }

    public void dispose() {
        if (phaseTimer != null) {
            phaseTimer.stop();
            phaseTimer = null;
        }
        if (returnToLobbyTimer != null) {
            returnToLobbyTimer.stop();
            returnToLobbyTimer = null;
        }
        model.removePropertyChangeListener(modelListener);
    }

    // ─────────────────────────── Construction de la scène ───────────────────────────

    private Scene buildScene() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DEEP + ";");

        headerPane = buildHeader();
        playerPanel = buildPlayerPanel();
        eventLogPanel = buildEventLog();
        actionPanelBox = buildActionPanel();

        root.setTop(headerPane);
        root.setLeft(playerPanel);
        root.setCenter(eventLogPanel);
        root.setBottom(actionPanelBox);

        return new Scene(root, 960, 680);
    }

    private HBox buildHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(14, 22, 14, 22));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + BG_HEADER + ";");

        phaseLabel = new Label("SALON");
        phaseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        phaseLabel.setTextFill(Color.web(GOLD));

        timerLabel = new Label("⏱ 00:00");
        timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        timerLabel.setTextFill(Color.web(GOLD));
        timerLabel.setVisible(false); // Caché par défaut dans le lobby

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        roleLabel = new Label("Rôle : ?");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        roleLabel.setTextFill(Color.web(MUTED_BLUE));

        header.getChildren().addAll(phaseLabel, timerLabel, spacer, roleLabel);
        return header;
    }

    private VBox buildPlayerPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(195);
        panel.setStyle("-fx-background-color: " + BG_PANEL + ";");

        Label title = new Label("Joueurs en vie");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        title.setTextFill(Color.web(GOLD));

        playerListView = new ListView<>();
        playerListView.setStyle(
            "-fx-background-color: " + BG_HEADER + "; -fx-control-inner-background: " + BG_HEADER + ";"
        );
        playerListView.setFocusTraversable(false);
        VBox.setVgrow(playerListView, Priority.ALWAYS);

        panel.getChildren().addAll(title, playerListView);
        return panel;
    }

    private VBox buildEventLog() {
        VBox logPanel = new VBox(8);
        logPanel.setPadding(new Insets(15));
        logPanel.setStyle("-fx-background-color: " + BG_DEEP + ";");

        Label title = new Label("Événements et Discussion");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        title.setTextFill(Color.web(MUTED_BLUE));

        eventLogView = new ListView<>();
        eventLogView.setStyle(
            "-fx-background-color: " + BG_PANEL + "; -fx-control-inner-background: " + BG_PANEL + ";"
        );
        VBox.setVgrow(eventLogView, Priority.ALWAYS);

        HBox chatInputBox = new HBox(10);
        chatInputBox.setAlignment(Pos.CENTER_LEFT);
        
        chatField = new TextField();
        chatField.setPromptText("Saisir un message...");
        chatField.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-text-fill: white;");
        HBox.setHgrow(chatField, Priority.ALWAYS);
        
        sendBtn = new Button("Envoyer");
        sendBtn.setStyle(buildButtonStyle(MUTED_BLUE));
        sendBtn.setOnAction(e -> {
            String msg = chatField.getText().trim();
            if (!msg.isEmpty()) {
                controller.sendChat(msg);
                chatField.clear();
            }
        });
        chatField.setOnAction(e -> sendBtn.fire());
        chatInputBox.getChildren().addAll(chatField, sendBtn);

        logPanel.getChildren().addAll(title, eventLogView, chatInputBox);
        return logPanel;
    }

    private VBox buildActionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(14, 22, 14, 22));
        panel.setPrefHeight(190);
        panel.setStyle(
            "-fx-background-color: " + BG_HEADER + ";"
            + "-fx-border-color: #3a3a6e; -fx-border-width: 2 0 0 0;"
        );

        actionPromptLabel = new Label("En attente du démarrage...");
        actionPromptLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        actionPromptLabel.setTextFill(Color.web(GOLD));

        HBox content = new HBox(16);
        content.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(content, Priority.ALWAYS);

        targetListView = new ListView<>();
        targetListView.setPrefWidth(300);
        targetListView.setPrefHeight(110);
        targetListView.setStyle(
            "-fx-background-color: " + BG_PANEL + "; -fx-control-inner-background: " + BG_PANEL + ";"
        );

        actionButton = new Button("Confirmer");
        actionButton.setPrefWidth(130);
        actionButton.setPrefHeight(45);
        actionButton.setStyle(buildButtonStyle(BTN_RED));
        actionButton.setDisable(true);
        actionButton.setOnAction(e -> handleActionClick());
        actionButton.hoverProperty().addListener((obs, wasHover, isHover) ->
            actionButton.setStyle(buildButtonStyle(isHover && !actionButton.isDisabled() ? BTN_ACTIVE : BTN_RED))
        );

        content.getChildren().addAll(targetListView, actionButton);
        panel.getChildren().addAll(actionPromptLabel, content);
        return panel;
    }

    private String buildButtonStyle(String bg) {
        return "-fx-background-color: " + bg + "; "
             + "-fx-text-fill: white; "
             + "-fx-font-size: 14px; "
             + "-fx-font-weight: bold; "
             + "-fx-background-radius: 6;";
    }

    // ─────────────────────────── Rafraîchissement de l'interface ───────────────────────────────────

    private void refreshUI() {
        GameState phase = model.getGamePhase();
        String myRole   = model.getMyRole();
        List<Player> alive = model.getAlivePlayers();
        boolean isAlive = alive.stream().anyMatch(p -> p.getUsername().equals(model.getMyUsername()));

        updateBackground(phase, isAlive);
        updateHeader(phase, myRole);
        updatePlayerList(alive);
        updateEventLog();
        updateActionPanel(phase, myRole);
        updateChatPanel(phase);
        if (phase == GameState.GAME_OVER) {
            appendGameOverToLog();
            scheduleReturnToLobby();
        }

        if (phase != currentPhase) {
            currentPhase = phase;
            startTimerForPhase(phase);
        }
    }

    private void updateBackground(GameState phase, boolean isAlive) {
        if (root == null) return;
        
        String deepColor, panelColor, headerColor;
        
        boolean inGamePhase = phase == GameState.NIGHT || phase == GameState.DAY_DISCUSSION || phase == GameState.DAY_VOTING;
        if (!isAlive && inGamePhase) {
            deepColor = "#1c1c1c";
            panelColor = "#2b2b2b";
            headerColor = "#383838";
        } else {
            switch (phase) {
                case NIGHT -> {
                    deepColor = "#05051a";
                    panelColor = "#0a0a2a";
                    headerColor = "#10103a";
                }
                case DAY_DISCUSSION, DAY_VOTING -> {
                    deepColor = "#2b3b5b";
                    panelColor = "#3b4b6b";
                    headerColor = "#4b5b7b";
                }
                case GAME_OVER -> {
                    deepColor = "#1a0a0a";
                    panelColor = "#2a1a1a";
                    headerColor = "#3a2a2a";
                }
                default -> {
                    deepColor = BG_DEEP;
                    panelColor = BG_PANEL;
                    headerColor = BG_HEADER;
                }
            }
        }
        
        root.setStyle("-fx-background-color: " + deepColor + ";");
        if (headerPane != null) headerPane.setStyle("-fx-background-color: " + headerColor + ";");
        if (playerPanel != null) playerPanel.setStyle("-fx-background-color: " + panelColor + ";");
        if (playerListView != null) playerListView.setStyle("-fx-background-color: " + headerColor + "; -fx-control-inner-background: " + headerColor + ";");
        if (eventLogPanel != null) eventLogPanel.setStyle("-fx-background-color: " + deepColor + ";");
        if (eventLogView != null) eventLogView.setStyle("-fx-background-color: " + panelColor + "; -fx-control-inner-background: " + panelColor + ";");
        if (chatField != null) chatField.setStyle("-fx-background-color: " + panelColor + "; -fx-text-fill: white;");
        if (actionPanelBox != null) actionPanelBox.setStyle("-fx-background-color: " + headerColor + "; -fx-border-color: #3a3a6e; -fx-border-width: 2 0 0 0;");
        if (targetListView != null) targetListView.setStyle("-fx-background-color: " + panelColor + "; -fx-control-inner-background: " + panelColor + ";");
    }

    private void updateHeader(GameState phase, String myRole) {
        String phaseText = switch (phase) {
            case NIGHT          -> "🌙 NUIT";
            case DAY_DISCUSSION -> "☀️ JOUR — Discussion";
            case DAY_VOTING     -> "🗳️ JOUR — Vote";
            case GAME_OVER      -> "🏁 FIN DE PARTIE";
            default             -> "SALON";
        };
        phaseLabel.setText(phaseText);

        if (myRole != null) {
            String roleEmoji = switch (myRole) {
                case "Werewolf" -> "🐺 ";
                case "Seer"     -> "👁️ ";
                case "Medic"    -> "💊 ";
                case "Villager" -> "🪓 ";
                default         -> "❓ ";
            };
            String roleColor = switch (myRole) {
                case "Werewolf" -> "#ff5555";
                case "Seer"     -> "#55aaff";
                case "Medic"    -> "#55ee88";
                case "Villager" -> MUTED_BLUE;
                default         -> "#ffffff";
            };
            roleLabel.setText(roleEmoji + myRole);
            roleLabel.setTextFill(Color.web(roleColor));
        } else {
            roleLabel.setText("Rôle : ?");
            roleLabel.setTextFill(Color.web(MUTED_BLUE));
        }
    }

    private void updatePlayerList(List<Player> alive) {
        List<String> names = alive.stream().map(Player::getUsername).toList();
        if (!playerListView.getItems().equals(names)) {
            playerListView.getItems().setAll(names);
        }
    }

    private void updateEventLog() {
        List<String> log = model.getEventLog();
        if (eventLogView.getItems().size() != log.size()) {
            eventLogView.getItems().setAll(log);
            if (!log.isEmpty()) {
                eventLogView.scrollTo(log.size() - 1);
            }
        }
    }

    private void startTimerForPhase(GameState phase) {
        if (phaseTimer != null) {
            phaseTimer.stop();
        }
        
        timeRemaining = switch (phase) {
            case NIGHT -> 30;           // 30 secondes pour la nuit
            case DAY_DISCUSSION -> 60;  // 60 secondes pour débattre
            case DAY_VOTING -> 30;      // 30 secondes pour voter
            default -> 0;
        };

        if (timeRemaining > 0) {
            timerLabel.setVisible(true);
            timerLabel.setText(formatTime(timeRemaining));
            phaseTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                timeRemaining--;
                if (timeRemaining <= 0) {
                    timeRemaining = 0;
                    phaseTimer.stop();
                }
                timerLabel.setText(formatTime(timeRemaining));
            }));
            phaseTimer.setCycleCount(Timeline.INDEFINITE);
            phaseTimer.play();
        } else {
            timerLabel.setVisible(false);
        }
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("⏱ %02d:%02d", m, s);
    }

    private void updateActionPanel(GameState phase, String myRole) {
        List<Player> alive = model.getAlivePlayers();
        boolean isAlive = alive.stream().anyMatch(p -> p.getUsername().equals(model.getMyUsername()));
        boolean canAct = model.isCanAct() && !model.isHasActedThisPhase() && isAlive;

        List<String> targets = alive.stream()
            .map(Player::getUsername)
            .filter(name -> !name.equals(model.getMyUsername()))
            .toList();

        switch (phase) {
            case NIGHT -> {
                if (myRole == null || "Villager".equals(myRole) || !canAct) {
                    actionPromptLabel.setText("🌙 Phase de nuit — fermez les yeux…");
                    hideTargetList();
                } else {
                    String prompt = switch (myRole) {
                        case "Werewolf" -> "🐺 Choisissez une victime à éliminer :";
                        case "Seer"     -> "👁️ Choisissez un joueur à investiguer :";
                        case "Medic"    -> "💊 Choisissez un joueur à protéger :";
                        default -> "🌙 Phase de nuit — fermez les yeux…";
                    };
                    actionPromptLabel.setText(prompt);
                    if (prompt.contains("fermez")) {
                        hideTargetList();
                    } else {
                        showTargetList(targets, "Confirmer");
                    }
                }
            }
            case DAY_DISCUSSION -> {
                actionPromptLabel.setText("☀️ Discutez avec le village. Le vote commence bientôt…");
                hideTargetList();
            }
            case DAY_VOTING -> {
                if (!isAlive) {
                    actionPromptLabel.setText("💀 Vous êtes éliminé. Vous ne pouvez pas voter.");
                    hideTargetList();
                } else if (canAct) {
                    actionPromptLabel.setText("🗳️ Sélectionnez un suspect puis cliquez sur Voter :");
                    showTargetList(targets, "Voter");
                } else {
                    actionPromptLabel.setText("🗳️ Vote envoyé. En attente des autres joueurs…");
                    hideTargetList();
                }
            }
            case GAME_OVER -> {
                actionPromptLabel.setText("🏁 La partie est terminée. Consultez le journal.");
                hideTargetList();
            }
            default -> {
                actionPromptLabel.setText("En attente…");
                hideTargetList();
            }
        }
    }

    private void updateChatPanel(GameState phase) {
        if (chatField == null || sendBtn == null) return;
        List<Player> alive = model.getAlivePlayers();
        boolean isAlive = alive.stream().anyMatch(p -> p.getUsername().equals(model.getMyUsername()));

        boolean canChat = isAlive && phase != GameState.NIGHT;
        chatField.setDisable(!canChat);
        sendBtn.setDisable(!canChat);

        if (!isAlive) {
            chatField.setPromptText("💀 Les morts ne parlent pas...");
        } else if (phase == GameState.NIGHT) {
            chatField.setPromptText("🌙 Le village dort...");
        } else {
            chatField.setPromptText("Saisir un message...");
        }
    }

    private void showTargetList(List<String> targets, String buttonLabel) {
        targetListView.setVisible(true);
        targetListView.setManaged(true);
        if (!targetListView.getItems().equals(targets)) {
            targetListView.getItems().setAll(targets);
        }
        actionButton.setText(buttonLabel);
        actionButton.setDisable(false);
        actionButton.setStyle(buildButtonStyle(BTN_RED));
    }

    private void hideTargetList() {
        targetListView.setVisible(false);
        targetListView.setManaged(false);
        actionButton.setDisable(true);
        actionButton.setStyle(buildButtonStyle(BTN_RED));
    }

    /** Ajoute les résultats de fin de partie au journal des événements (une seule fois). */
    private void appendGameOverToLog() {
        String winner = model.getGameOverWinner();
        Map<String, String> allRoles = model.getAllRoles();

        if (winner != null && !eventLogView.getItems().contains("🏆 " + winner)) {
            eventLogView.getItems().add("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            eventLogView.getItems().add("🏆 " + winner);
            if (!allRoles.isEmpty()) {
                eventLogView.getItems().add("—— Rôles révélés ——");
                allRoles.forEach((username, role) ->
                    eventLogView.getItems().add("  " + username + "  →  " + role)
                );
            }
            eventLogView.getItems().add("━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            eventLogView.scrollTo(eventLogView.getItems().size() - 1);
        }
    }

    private void scheduleReturnToLobby() {
        if (gameOverHandled) return;
        gameOverHandled = true;

        returnToLobbyTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            dispose();
            if (onGameOverClose != null) {
                onGameOverClose.run();
            }
        }));
        returnToLobbyTimer.setCycleCount(1);
        returnToLobbyTimer.play();
    }

    // ─────────────────────────── Gestionnaire d'actions ───────────────────────────────

    private void handleActionClick() {
        String selected = targetListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            actionPromptLabel.setText("⚠️ Veuillez sélectionner un joueur !");
            return;
        }

        GameState phase = model.getGamePhase();
        if (phase == GameState.NIGHT) {
            controller.performNightAction(selected);
        } else if (phase == GameState.DAY_VOTING) {
            controller.performVote(selected);
        }
    }
}
