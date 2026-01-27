package domain.role;

import domain.role.impl.VillagerRole;
import domain.role.impl.WerewolfRole;
import java.util.*;

/**
 * Factory pour créer les rôles - Principe DIP (Dependency Inversion)
 * Le code dépend de l'interface Role, pas des implémentations concrètes
 */
public class RoleFactory {
    
    private static final Map<String, Role> ROLE_PROTOTYPES = new HashMap<>();
    
    static {
        registerRole("WEREWOLF", new WerewolfRole());
        registerRole("VILLAGER", new VillagerRole());
    }

    /**
     * Enregistre un nouveau type de rôle (extensibilité)
     */
    public static void registerRole(String type, Role role) {
        ROLE_PROTOTYPES.put(type.toUpperCase(), role);
    }

    /**
     * Crée une instance de rôle par son type
     */
    public static Role createRole(String type) {
        Role prototype = ROLE_PROTOTYPES.get(type.toUpperCase());
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown role type: " + type);
        }
        return prototype; // Dans un cas réel, on utiliserait le pattern Prototype pour cloner
    }

    /**
     * Distribue les rôles selon la configuration
     */
    public static List<Role> distributeRoles(int totalPlayers, int werewolvesCount) {
        List<Role> roles = new ArrayList<>();
        
        // Ajouter les loups-garous
        for (int i = 0; i < werewolvesCount; i++) {
            roles.add(createRole("WEREWOLF"));
        }
        
        // Compléter avec des villageois
        for (int i = werewolvesCount; i < totalPlayers; i++) {
            roles.add(createRole("VILLAGER"));
        }
        
        // Mélanger les rôles
        Collections.shuffle(roles);
        
        return roles;
    }

    /**
     * Retourne tous les types de rôles disponibles
     */
    public static Set<String> getAvailableRoles() {
        return ROLE_PROTOTYPES.keySet();
    }
}
