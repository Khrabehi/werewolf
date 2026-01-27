package domain.model;

import java.util.*;

/**
 * Value Object pour gérer les sessions de vote - Principe SRP
 */
public class VoteSession {
    private final Map<String, Integer> votes;
    private final Set<String> voters;

    public VoteSession() {
        this.votes = new HashMap<>();
        this.voters = new HashSet<>();
    }

    public void registerVote(String voterId, String targetPseudo) {
        if (voters.contains(voterId)) {
            throw new IllegalStateException("Player has already voted");
        }
        votes.put(targetPseudo, votes.getOrDefault(targetPseudo, 0) + 1);
        voters.add(voterId);
    }

    public boolean hasVoted(String voterId) {
        return voters.contains(voterId);
    }

    public Optional<String> getWinner() {
        if (votes.isEmpty()) {
            return Optional.empty();
        }

        int maxVotes = Collections.max(votes.values());
        List<String> topVoted = votes.entrySet().stream()
                .filter(e -> e.getValue() == maxVotes)
                .map(Map.Entry::getKey)
                .toList();

        // En cas d'égalité, choisir aléatoirement
        return Optional.of(topVoted.get(new Random().nextInt(topVoted.size())));
    }

    public void clear() {
        votes.clear();
        voters.clear();
    }

    public int getVoteCount() {
        return voters.size();
    }

    public Map<String, Integer> getVotes() {
        return Collections.unmodifiableMap(votes);
    }
}
