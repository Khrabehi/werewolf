module com.werewolf {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.werewolf to javafx.fxml;
    exports com.werewolf;
}
