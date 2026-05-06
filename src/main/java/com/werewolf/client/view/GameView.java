package com.werewolf.client.view;

import com.werewolf.client.controller.GameViewController;
import com.werewolf.client.model.GameModel;
import com.werewolf.game.GameState;
import com.werewolf.game.Player;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
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

    // Header
    private Label phaseLabel;
    private Label roleLabel;

    // Player sidebar
    private ListView<String> playerListView;

    // Event log
    private ListView<String> eventLogView;

    // Action panel
    private Label actionPromptLabel;
    private ListView<String> targetListView;
    private Button actionButton;

    public GameView(Stage stage, GameModel model, GameViewController controller) {
        this.stage = stage;
        this.model = model;
        this.controller = controller;
    }

    public void show() {
        Scene scene = buildScene();
        stage.setScene(scene);
        stage.setWidth(960);
        stage.setHeight(680);
        stage.setResizable(true);
        stage.setTitle("Werewolf – In Game");

        model.addPropertyChangeListener(evt -> Platform.runLater(this::refreshUI));
        refreshUI();
    }

    // ─────────────────────────── Scene construction ───────────────────────────

    private Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DEEP + ";");

        root.setTop(buildHeader());
        root.setLeft(buildPlayerPanel());
        root.setCenter(buildEventLog());
        root.setBottom(buildActionPanel());

        return new Scene(root, 960, 680);
    }

    private HBox buildHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(14, 22, 14, 22));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + BG_HEADER + ";");

        phaseLabel = new Label("LOBBY");
        phaseLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        phaseLabel.setTextFill(Color.web(GOLD));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        roleLabel = new Label("Rôle : ?");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        roleLabel.setTextFill(Color.web(MUTED_BLUE));

        header.getChildren().addAll(phaseLabel, spacer, roleLabel);
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

        Label title = new Label("Événements");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        title.setTextFill(Color.web(MUTED_BLUE));

        eventLogView = new ListView<>();
        eventLogView.setStyle(
            "-fx-background-color: " + BG_PANEL + "; -fx-control-inner-background: " + BG_PANEL + ";"
        );
        VBox.setVgrow(eventLogView, Priority.ALWAYS);

        logPanel.getChildren().addAll(title, eventLogView);
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

    // ─────────────────────────── UI refresh ───────────────────────────────────

    private void refreshUI() {
        GameState phase = model.getGamePhase();
        String myRole   = model.getMyRole();
        List<Player> alive = model.getAlivePlayers();

        updateHeader(phase, myRole);
        updatePlayerList(alive);
        updateEventLog();
        updateActionPanel(phase, myRole);
        if (phase == GameState.GAME_OVER) appendGameOverToLog();
    }

    private void updateHeader(GameState phase, String myRole) {
        String phaseText = switch (phase) {
            case NIGHT          -> "🌙 NUIT";
            case DAY_DISCUSSION -> "☀️ JOUR — Discussion";
            case DAY_VOTING     -> "🗳️ JOUR — Vote";
            case GAME_OVER      -> "🏁 FIN DE PARTIE";
            default             -> "LOBBY";
        };
        phaseLabel.setText(phaseText);

        if (myRole != null) {
            String roleEmoji = switch (myRole) {
                case "Werewolf" -> "🐺 ";
                case "Seer"     -> "👁️ ";
                case "Medic"    -> "💊 ";
                default         -> "🪓 ";
            };
            String roleColor = switch (myRole) {
                case "Werewolf" -> "#ff5555";
                case "Seer"     -> "#55aaff";
                case "Medic"    -> "#55ee88";
                default         -> MUTED_BLUE;
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

    private void updateActionPanel(GameState phase, String myRole) {
        boolean canAct = model.isCanAct() && !model.isHasActedThisPhase();
        List<Player> alive = model.getAlivePlayers();
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
                        default -> "";
                    };
                    actionPromptLabel.setText(prompt);
                    showTargetList(targets, "Confirmer");
                }
            }
            case DAY_DISCUSSION -> {
                actionPromptLabel.setText("☀️ Discutez avec le village. Le vote commence bientôt…");
                hideTargetList();
            }
            case DAY_VOTING -> {
                if (canAct) {
                    actionPromptLabel.setText("🗳️ Votez pour éliminer un suspect :");
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

    /** Appends game-over results to the event log (only once). */
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

    // ─────────────────────────── Action handler ───────────────────────────────

    private void handleActionClick() {
        String selected = targetListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        GameState phase = model.getGamePhase();
        if (phase == GameState.NIGHT) {
            controller.performNightAction(selected);
        } else if (phase == GameState.DAY_VOTING) {
            controller.performVote(selected);
        }
    }
}
