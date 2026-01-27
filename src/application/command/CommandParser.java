package application.command;

import application.command.impl.KillVoteCommand;
import application.command.impl.SetPseudoCommand;
import application.command.impl.StartGameCommand;

/**
 * Factory pour créer les commandes à partir des inputs utilisateur
 * Principe OCP: on peut ajouter de nouvelles commandes sans modifier ce code
 */
public class CommandParser {

    /**
     * Parse une ligne de commande et retourne la commande correspondante
     */
    public static GameCommand parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String command = parts[0].toUpperCase();

        switch (command) {
            case "PSEUDO":
                if (parts.length < 2) {
                    return null;
                }
                return new SetPseudoCommand(parts[1].trim());

            case "START":
                return new StartGameCommand();

            case "KILL":
                if (parts.length < 2) {
                    return null;
                }
                return new KillVoteCommand(parts[1].trim());

            default:
                return null;
        }
    }
}
