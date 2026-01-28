package domain.role;

import domain.role.impl.VillagerRole;
import domain.role.impl.WerewolfRole;
import java.util.*;

/**
 * Factory to create roles - DIP Principle (Dependency Inversion)
 * The code depends on the Role interface, not concrete implementations
 */
public class RoleFactory {
    
    private static final Map<String, Role> ROLE_PROTOTYPES = new HashMap<>();
    
    static {
        registerRole("WEREWOLF", new WerewolfRole());
        registerRole("VILLAGER", new VillagerRole());
    }

    /**
     * Registers a new role type (extensibility)
     */
    public static void registerRole(String type, Role role) {
        ROLE_PROTOTYPES.put(type.toUpperCase(), role);
    }

    /**
     * Creates a role instance by its type
     */
    public static Role createRole(String type) {
        Role prototype = ROLE_PROTOTYPES.get(type.toUpperCase());
        if (prototype == null) {
            throw new IllegalArgumentException("Unknown role type: " + type);
        }
        return prototype; // In a real case, we would use the Prototype pattern to clone
    }

    /**
     * Distributes roles according to configuration
     */
    public static List<Role> distributeRoles(int totalPlayers, int werewolvesCount) {
        List<Role> roles = new ArrayList<>();
        
        // Add werewolves
        for (int i = 0; i < werewolvesCount; i++) {
            roles.add(createRole("WEREWOLF"));
        }
        
        // Fill with villagers
        for (int i = werewolvesCount; i < totalPlayers; i++) {
            roles.add(createRole("VILLAGER"));
        }
        
        // Shuffle the roles
        Collections.shuffle(roles);
        
        return roles;
    }

    /**
     * Returns all available role types
     */
    public static Set<String> getAvailableRoles() {
        return ROLE_PROTOTYPES.keySet();
    }
}
