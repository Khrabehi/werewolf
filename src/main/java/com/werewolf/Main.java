package com.werewolf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static void configureGraphicsFallback() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("linux") && System.getProperty("prism.order") == null) {
            System.setProperty("prism.order", "sw");
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/werewolf/GameView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);

        primaryStage.setTitle("Werewolf – Night Phase");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        configureGraphicsFallback();
        launch(args);
    }
}
