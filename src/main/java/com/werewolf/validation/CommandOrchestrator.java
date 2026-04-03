package com.werewolf.validation;

import com.werewolf.game.GameSession;
import com.werewolf.game.Player;
import com.werewolf.game.action.ActionFactory;
import com.werewolf.game.action.GameAction;
import com.werewolf.network.shared.GameCommand;

public class CommandOrchestrator {
    private Validator validationChain;
    private ActionFactory actionFactory;
    private GameSession gameSession;
    
    public CommandOrchestrator(GameSession gameSession) {
        this.gameSession = gameSession;
        this.actionFactory = new ActionFactory();
        buildValidationChain();
    }
    
    private void buildValidationChain() {
        Validator validator = new PlayerExistenceValidator();
        validator.setNext(new PlayerAliveValidator())
                 .setNext(new RolePermissionValidator())
                 .setNext(new GameStateValidator())
                 .setNext(new TargetValidationValidator());
                 
        this.validationChain = validator;
    }

    public CommandExecutionResult executeCommand(String playerId, GameCommand cmd) {
        Player actor = gameSession.getPlayer(playerId);
        
        ValidationResult validation = validationChain.validate(cmd, actor, gameSession);
        if (!validation.isValid()) {
            return CommandExecutionResult.failed(validation.getErrorMessage());
        }
        
        try {
            Player target = gameSession.getPlayer(cmd.getTargetPlayerId());
            GameAction action = actionFactory.getAction(cmd.getActionType());
            action.execute(actor, target, gameSession);
            
            return CommandExecutionResult.success();
        } catch (Exception e) {
            return CommandExecutionResult.failed("Execution error: " + e.getMessage());
        }
    }
}