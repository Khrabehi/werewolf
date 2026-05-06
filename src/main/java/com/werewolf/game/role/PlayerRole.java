package com.werewolf.game.role;

import java.io.Serializable;
import java.util.List;

public interface PlayerRole extends Serializable {
    String getName();

    /**
        * Détermine si ce rôle autorise le type d'action spécifié.
        *
        * @param actionType le type d'action à vérifier pour ce rôle
        * @return {@code true} si ce rôle peut effectuer le type d'action spécifié ; {@code false} sinon
     */
    boolean canPerform(String actionType);

    List<String> getAllowedActions();
}
