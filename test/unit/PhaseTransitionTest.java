package unit;

import domain.model.Game;
import domain.phase.GamePhase;
import domain.phase.impl.NightPhase;
import domain.phase.impl.DayPhase;
import domain.phase.impl.EndPhase;
import application.event.GameEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests des transitions de phases
 */
public class PhaseTransitionTest {
    
    private Game game;
    
    @BeforeEach
    void setUp() {
        game = new Game(4, 10);
    }
    
    @Test
    @DisplayName("NightPhase -> DayPhase")
    void testNightToDayTransition() {
        NightPhase nightPhase = new NightPhase();
        
        List<GameEvent> startEvents = nightPhase.start(game);
        assertFalse(startEvents.isEmpty());
        assertTrue(startEvents.stream().anyMatch(e -> e.getMessage().contains("nuit")));
        
        GamePhase nextPhase = nightPhase.next(game);
        assertTrue(nextPhase instanceof DayPhase);
        assertEquals("DAY", nextPhase.getName());
        
        System.out.println("✅ Transition NIGHT → DAY validée");
    }
    
    @Test
    @DisplayName("DayPhase -> NightPhase ou EndPhase")
    void testDayTransition() {
        DayPhase dayPhase = new DayPhase();
        
        List<GameEvent> startEvents = dayPhase.start(game);
        assertFalse(startEvents.isEmpty());
        
        GamePhase nextPhase = dayPhase.next(game);
        // Dépend de la victoire ou non
        assertTrue(nextPhase instanceof NightPhase || nextPhase instanceof EndPhase);
        
        System.out.println("✅ Transition DAY testée");
    }
    
    @Test
    @DisplayName("EndPhase est terminale")
    void testEndPhaseIsTerminal() {
        EndPhase endPhase = new EndPhase();
        
        assertEquals("END", endPhase.getName());
        
        List<GameEvent> startEvents = endPhase.start(game);
        assertTrue(startEvents.stream().anyMatch(e -> e.getMessage().contains("gagné")));
        
        // EndPhase ne doit pas avoir de phase suivante (ou retourne elle-même)
        GamePhase nextPhase = endPhase.next(game);
        assertTrue(nextPhase instanceof EndPhase);
        
        System.out.println("✅ EndPhase terminale validée");
    }
    
    @Test
    @DisplayName("canEnd retourne toujours vrai par défaut")
    void testCanEndDefaultBehavior() {
        NightPhase nightPhase = new NightPhase();
        assertTrue(nightPhase.canEnd(game));
        
        DayPhase dayPhase = new DayPhase();
        assertTrue(dayPhase.canEnd(game));
        
        System.out.println("✅ canEnd fonctionne correctement");
    }
}
