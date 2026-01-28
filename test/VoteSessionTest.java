import domain.model.VoteSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the voting system
 */
class VoteSessionTest {

    private VoteSession voteSession;

    @BeforeEach
    void setUp() {
        voteSession = new VoteSession();
    }

    @Test
    void testRegisterVote() {
        voteSession.registerVote("voter1", "Alice");
        
        assertEquals(1, voteSession.getVoteCount());
        assertTrue(voteSession.hasVoted("voter1"));
    }

    @Test
    void testCannotVoteTwice() {
        voteSession.registerVote("voter1", "Alice");
        
        assertThrows(IllegalStateException.class, 
            () -> voteSession.registerVote("voter1", "Bob"));
    }

    @Test
    void testGetWinnerWithSingleVote() {
        voteSession.registerVote("voter1", "Alice");
        
        assertTrue(voteSession.getWinner().isPresent());
        assertEquals("Alice", voteSession.getWinner().get());
    }

    @Test
    void testGetWinnerWithMultipleVotes() {
        voteSession.registerVote("voter1", "Alice");
        voteSession.registerVote("voter2", "Alice");
        voteSession.registerVote("voter3", "Bob");
        
        assertEquals("Alice", voteSession.getWinner().get());
    }

    @Test
    void testGetWinnerWithTie() {
        voteSession.registerVote("voter1", "Alice");
        voteSession.registerVote("voter2", "Bob");
        
        // In case of a tie, a random winner is chosen
        String winner = voteSession.getWinner().get();
        assertTrue(winner.equals("Alice") || winner.equals("Bob"));
    }

    @Test
    void testClearVotes() {
        voteSession.registerVote("voter1", "Alice");
        voteSession.registerVote("voter2", "Bob");
        
        voteSession.clear();
        
        assertEquals(0, voteSession.getVoteCount());
        assertFalse(voteSession.hasVoted("voter1"));
        assertTrue(voteSession.getWinner().isEmpty());
    }

    @Test
    void testGetWinnerWhenNoVotes() {
        assertTrue(voteSession.getWinner().isEmpty());
    }
}
