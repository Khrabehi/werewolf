package com.werewolf.client.controller;

import com.werewolf.client.model.ConnectionConfig;
import com.werewolf.client.model.MainMenuModel;
import com.werewolf.client.network.ConnectionManager;
import com.werewolf.client.validation.ClientValidator;
import com.werewolf.client.validation.IpAddressValidator;
import com.werewolf.client.validation.PortValidator;
import com.werewolf.client.validation.UsernameValidator;
import com.werewolf.validation.ValidationResult;

public class MainMenuController {
    private final MainMenuModel model;
    private final ClientValidator<String> usernameValidator;
    private final ClientValidator<String> ipValidator;
    private final ClientValidator<String> portValidator;
    private final ConnectionManager connectionManager;

    public MainMenuController(MainMenuModel model) {
        this.model = model;
        this.usernameValidator = new UsernameValidator();
        this.ipValidator = new IpAddressValidator();
        this.portValidator = new PortValidator();
        this.connectionManager = new ConnectionManager(model, this::onConnectionResult);
    }

    public ValidationResult validateInputs() {
        ValidationResult usernameResult = usernameValidator.validate(model.getUsername());
        if (!usernameResult.isValid()) {
            model.setStatusMessage(usernameResult.getErrorMessage());
            return usernameResult;
        }

        ValidationResult ipResult = ipValidator.validate(model.getIpAddress());
        if (!ipResult.isValid()) {
            model.setStatusMessage(ipResult.getErrorMessage());
            return ipResult;
        }

        ValidationResult portResult = portValidator.validate(model.getPort());
        if (!portResult.isValid()) {
            model.setStatusMessage(portResult.getErrorMessage());
            return portResult;
        }

        return ValidationResult.VALID();
    }

    public void connectToServer() {
        if (model.isConnecting()) {
            return;
        }

        ValidationResult validationResult = validateInputs();
        if (!validationResult.isValid()) {
            return;
        }

        model.setIsConnecting(true);

        ConnectionConfig config = new ConnectionConfig(
            model.getUsername(),
            model.getIpAddress(),
            Integer.parseInt(model.getPort())
        );

        connectionManager.connectAsync(config);
    }

    public void quitApplication() {
        connectionManager.disconnect();
        System.exit(0);
    }

    public void startGame() {
        connectionManager.sendStartGame(model.getUsername());
    }

    public MainMenuModel getModel() {
        return model;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private void onConnectionResult(boolean success) {
        if (success) {
            System.out.println("Successfully connected as: " + model.getUsername());
        }
    }
}
