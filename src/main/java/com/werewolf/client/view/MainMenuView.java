package com.werewolf.client.view;

import com.werewolf.client.controller.MainMenuController;
import com.werewolf.client.model.MainMenuModel;

import java.beans.PropertyChangeEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenuView extends Application {
    private MainMenuModel model;
    private MainMenuController controller;

    private TextField usernameField;
    private TextField ipField;
    private TextField portField;
    private Button joinButton;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        model = new MainMenuModel();
        controller = new MainMenuController(model);

        Scene scene = createMainMenuScene();

        primaryStage.setTitle("Werewolf - The Village");
        primaryStage.setScene(scene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.setResizable(false);

        model.addPropertyChangeListener(this::onModelPropertyChange);

        primaryStage.show();
    }

    private Scene createMainMenuScene() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 12;");

        Label titleLabel = new Label("Werewolf - The Village");
        titleLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        root.getChildren().add(titleLabel);

        root.getChildren().add(new Separator());
        root.getChildren().add(createInputForm());
        root.getChildren().add(new Separator());
        root.getChildren().add(createButtons());

        statusLabel = new Label(model.getStatusMessage());
        statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11; -fx-font-style: italic;");
        statusLabel.setWrapText(true);
        root.getChildren().add(statusLabel);

        return new Scene(root);
    }

    private VBox createInputForm() {
        VBox formBox = new VBox(15);
        formBox.setStyle("-fx-border-color: #ecf0f1; -fx-border-radius: 5; -fx-padding: 20;");

        HBox usernameBox = new HBox(10);
        Label usernameLabel = new Label("Username");
        usernameLabel.setPrefWidth(80);
        usernameLabel.setStyle("-fx-font-weight: bold;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username (max 16 chars)");
        usernameField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(usernameField, Priority.ALWAYS);
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> model.setUsername(newVal));
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        formBox.getChildren().add(usernameBox);

        HBox ipBox = new HBox(10);
        Label ipLabel = new Label("IP Address");
        ipLabel.setPrefWidth(80);
        ipLabel.setStyle("-fx-font-weight: bold;");
        ipField = new TextField(model.getIpAddress());
        ipField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(ipField, Priority.ALWAYS);
        ipField.textProperty().addListener((obs, oldVal, newVal) -> model.setIpAddress(newVal));
        ipBox.getChildren().addAll(ipLabel, ipField);
        formBox.getChildren().add(ipBox);

        HBox portBox = new HBox(10);
        Label portLabel = new Label("Port");
        portLabel.setPrefWidth(80);
        portLabel.setStyle("-fx-font-weight: bold;");
        portField = new TextField(model.getPort());
        portField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(portField, Priority.ALWAYS);
        portField.textProperty().addListener((obs, oldVal, newVal) -> model.setPort(newVal));
        portBox.getChildren().addAll(portLabel, portField);
        formBox.getChildren().add(portBox);

        return formBox;
    }

    private HBox createButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        joinButton = new Button("Join Game");
        joinButton.setPrefWidth(120);
        joinButton.setPrefHeight(40);
        joinButton.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        joinButton.setOnAction(e -> controller.connectToServer());

        Button quitButton = new Button("Quit");
        quitButton.setPrefWidth(120);
        quitButton.setPrefHeight(40);
        quitButton.setStyle("-fx-font-size: 14;");
        quitButton.setOnAction(e -> controller.quitApplication());

        buttonBox.getChildren().addAll(joinButton, quitButton);
        return buttonBox;
    }

    private void onModelPropertyChange(PropertyChangeEvent event) {
        Platform.runLater(() -> {
            if ("statusMessage".equals(event.getPropertyName())) {
                statusLabel.setText((String) event.getNewValue());
                return;
            }

            if ("isConnecting".equals(event.getPropertyName())) {
                boolean isConnecting = (boolean) event.getNewValue();
                joinButton.setDisable(isConnecting);
                if (isConnecting) {
                    statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11; -fx-font-style: italic;");
                } else {
                    statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11; -fx-font-style: italic;");
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
