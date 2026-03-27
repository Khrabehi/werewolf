module com.werewolf {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.werewolf to javafx.fxml;
    opens com.werewolf.network.shared to java.base;
    exports com.werewolf;
    exports com.werewolf.network.shared;
    exports com.werewolf.network.server;
    exports com.werewolf.network.client;
    exports com.werewolf.security;
}
