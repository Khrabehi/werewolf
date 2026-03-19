package com.werewolf;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class GameController {

    @FXML
    private Label phaseLabel;

    @FXML
    private Button startButton;

    @FXML
    public void initialize() {
        // Initial label text is set in GameView.fxml
    }

    @FXML
    private void handleStartGame() {
        startButton.setDisable(true);
        phaseLabel.setText("The game has begun. Werewolves, choose your victim!");
    }
}
