module com.werewolf {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.werewolf to javafx.fxml;
    opens com.werewolf.network.shared;
    opens com.werewolf.validation;
    opens com.werewolf.game;
    opens com.werewolf.game.action;
    exports com.werewolf;
    exports com.werewolf.event;
    exports com.werewolf.game;
    exports com.werewolf.game.action;
    exports com.werewolf.game.role;
    exports com.werewolf.validation;
    exports com.werewolf.network.shared;
    exports com.werewolf.network.server;
    exports com.werewolf.network.client;
    exports com.werewolf.security;
    exports com.werewolf.client.view to javafx.graphics;
}
