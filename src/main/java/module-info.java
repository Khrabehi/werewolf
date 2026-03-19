module com.werewolf {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.werewolf to javafx.fxml;
    exports com.werewolf;
    exports com.werewolf.network.shared;
    exports com.werewolf.network.server;
    exports com.werewolf.network.client;
}
