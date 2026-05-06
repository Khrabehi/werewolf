package com.werewolf.game;

import com.werewolf.event.GameStateUpdate;
import com.werewolf.game.role.MedicRole;
import com.werewolf.game.role.PlayerRole;
import com.werewolf.game.role.SeerRole;
import com.werewolf.game.role.VillagerRole;
import com.werewolf.game.role.WerewolfRole;
import com.werewolf.network.server.PlayerConnectionManager;
import com.werewolf.network.shared.GameCommand;
import com.werewolf.network.shared.Message;
import com.werewolf.network.shared.MessageType;
import com.werewolf.validation.CommandExecutionResult;
import com.werewolf.validation.CommandOrchestrator;
import com.werewolf.validation.ValidationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameManager {
    private static final long NIGHT_DURATION_MS = 30000;
    private static final long DAY_DISCUSSION_MS = 45000;
    private static final long DAY_VOTING_MS = 30000;

    private final GameSession session;
    private final CommandOrchestrator orchestrator;
    private final ScheduledExecutorService scheduler;
    private final Random random;

    private ScheduledFuture<?> phaseTimer;

    private final Map<String, String> nightKills = new ConcurrentHashMap<>();
    private final Map<String, String> nightHeals = new ConcurrentHashMap<>();
    private final Map<String, String> nightPeeks = new ConcurrentHashMap<>();

    public GameManager(GameSession session) {
        this.session = session;
        this.orchestrator = new CommandOrchestrator(session);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.random = new Random();
    }

    public CommandExecutionResult startGame(String requesterId) {
        if (session.getCurrentPhase() != GameState.LOBBY) {
            return CommandExecutionResult.failed("Game already started");
        }
        if (requesterId == null || !requesterId.equals(session.getAdminId())) {
            return CommandExecutionResult.failed("Only the admin can start the game");
        }
        if (session.getAlivePlayers().size() < 3) {
            return CommandExecutionResult.failed("Not enough players to start (min 3)");
        }

        assignRoles();
        broadcastGameStarted();
        transitionTo(GameState.NIGHT, "Night falls. All villagers close their eyes.");
        return CommandExecutionResult.success();
    }

    private void broadcastGameStarted() {
        Message started = new Message(MessageType.GAME_STARTED, "Server", "Game started");
        PlayerConnectionManager.broadcastToAll(started);
    }

    public CommandExecutionResult handleCommand(String playerId, GameCommand cmd) {
        ValidationResult validation = orchestrator.validateCommand(playerId, cmd);
        if (!validation.isValid()) {
            return CommandExecutionResult.failed(validation.getErrorMessage());
        }

        String actionType = cmd.getActionType().toUpperCase();
        switch (actionType) {
            case "KILL" -> {
                nightKills.put(playerId, cmd.getTargetPlayerId());
                if (nightActionsComplete()) {
                    resolveNightPhase();
                }
            }
            case "HEAL" -> {
                nightHeals.put(playerId, cmd.getTargetPlayerId());
                if (nightActionsComplete()) {
                    resolveNightPhase();
                }
            }
            case "PEEK" -> {
                nightPeeks.put(playerId, cmd.getTargetPlayerId());
                if (nightActionsComplete()) {
                    resolveNightPhase();
                }
            }
            case "VOTE" -> {
                session.recordVote(playerId, cmd.getTargetPlayerId());
                if (votesComplete()) {
                    resolveVotingPhase();
                }
            }
            default -> {
                return CommandExecutionResult.failed("Unsupported action type: " + actionType);
            }
        }

        return CommandExecutionResult.success();
    }

    private void transitionTo(GameState nextState, String message) {
        stopPhaseTimer();
        if (nextState == GameState.NIGHT) {
            resetNightState();
            session.updatePhase(nextState, message);
            sendNightPrompts();
            schedulePhaseTimer(this::resolveNightPhase, NIGHT_DURATION_MS);
            return;
        }
        if (nextState == GameState.DAY_DISCUSSION) {
            session.updatePhase(nextState, message);
            schedulePhaseTimer(this::startVotingPhase, DAY_DISCUSSION_MS);
            return;
        }
        if (nextState == GameState.DAY_VOTING) {
            session.resetVotes();
            session.updatePhase(nextState, message);
            schedulePhaseTimer(this::resolveVotingPhase, DAY_VOTING_MS);
            return;
        }
        if (nextState == GameState.GAME_OVER) {
            session.updatePhase(nextState, message);
        }
    }

    private void startVotingPhase() {
        if (session.getCurrentPhase() != GameState.DAY_DISCUSSION) {
            return;
        }
        transitionTo(GameState.DAY_VOTING, "Voting begins. Choose who to eliminate.");
    }

    private void resolveNightPhase() {
        if (session.getCurrentPhase() != GameState.NIGHT) {
            return;
        }
        stopPhaseTimer();

        applyNightProtections();
        Player killed = resolveWerewolfKill();
        sendSeerResults();

        String summaryMessage = (killed != null)
            ? "Day breaks. " + killed.getUsername() + " was found dead."
            : "Day breaks. No one died last night.";

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("killedPlayer", killed != null ? killed.getUsername() : null);
        session.notifySessionUpdate(summaryMessage, metadata);

        if (checkWinConditions()) {
            return;
        }

        transitionTo(GameState.DAY_DISCUSSION, "The village wakes up and begins to discuss.");
    }

    private void resolveVotingPhase() {
        if (session.getCurrentPhase() != GameState.DAY_VOTING) {
            return;
        }
        stopPhaseTimer();

        Map<String, String> votes = session.getCurrentVotes();
        if (votes.isEmpty()) {
            session.notifySessionUpdate("No votes were cast. No one is eliminated.");
            if (checkWinConditions()) {
                return;
            }
            transitionTo(GameState.NIGHT, "Night falls. All villagers close their eyes.");
            return;
        }

        Map<String, Integer> tally = tallyVotes(votes);
        List<String> topTargets = resolveTopTargets(tally);

        if (topTargets.size() != 1) {
            session.notifySessionUpdate("Vote tie. No one is eliminated.");
            if (checkWinConditions()) {
                return;
            }
            transitionTo(GameState.NIGHT, "Night falls. All villagers close their eyes.");
            return;
        }

        Player eliminated = session.getPlayer(topTargets.get(0));
        if (eliminated != null) {
            eliminated.setAlive(false);
            String roleName = eliminated.getRole() != null ? eliminated.getRole().getName() : "Unknown";
            session.notifySessionUpdate(
                "The village eliminated " + eliminated.getUsername() + ". Role: " + roleName + "."
            );
        }

        if (checkWinConditions()) {
            return;
        }

        transitionTo(GameState.NIGHT, "Night falls. All villagers close their eyes.");
    }

    private void applyNightProtections() {
        for (String targetId : nightHeals.values()) {
            Player target = session.getPlayer(targetId);
            if (target != null && target.isAlive()) {
                target.setProtected(true);
            }
        }
    }

    private Player resolveWerewolfKill() {
        if (nightKills.isEmpty()) {
            return null;
        }

        Map<String, Integer> tally = tallyVotes(nightKills);
        List<String> topTargets = resolveTopTargets(tally);
        if (topTargets.isEmpty()) {
            return null;
        }

        String targetId = topTargets.get(random.nextInt(topTargets.size()));
        Player target = session.getPlayer(targetId);
        if (target == null || !target.isAlive()) {
            return null;
        }

        if (target.isProtected()) {
            return null;
        }

        target.setAlive(false);
        return target;
    }

    private void sendSeerResults() {
        for (Map.Entry<String, String> entry : nightPeeks.entrySet()) {
            Player seer = session.getPlayer(entry.getKey());
            Player target = session.getPlayer(entry.getValue());
            if (seer == null || target == null) {
                continue;
            }
            String roleName = target.getRole() != null ? target.getRole().getName() : "Unknown";
            GameStateUpdate update = new GameStateUpdate(
                "Your vision reveals that " + target.getUsername() + " is " + roleName + ".",
                session.getCurrentPhase(),
                session.getAlivePlayers()
            );
            sendPrivateUpdate(seer.getId(), update);
        }
    }

    private void sendNightPrompts() {
        for (Player player : session.getAlivePlayers()) {
            if (player.getRole() == null) {
                continue;
            }
            String roleName = player.getRole().getName();
            String prompt = switch (roleName) {
                case "Werewolf" -> "Werewolves wake up and choose a target.";
                case "Seer" -> "Seer, choose a player to investigate.";
                case "Medic" -> "Medic, choose a player to protect.";
                default -> null;
            };
            if (prompt != null) {
                GameStateUpdate update = new GameStateUpdate(
                    prompt,
                    session.getCurrentPhase(),
                    session.getAlivePlayers()
                );
                update.addMetadata("prompt", true);
                sendPrivateUpdate(player.getId(), update);
            }
        }
    }

    private void sendPrivateUpdate(String playerId, GameStateUpdate update) {
        Message message = new Message(MessageType.GAME_STATE_UPDATE, "Server", update);
        PlayerConnectionManager.sendToPlayer(playerId, message);
    }

    private boolean nightActionsComplete() {
        long werewolves = countAliveRole("Werewolf");
        long seers = countAliveRole("Seer");
        long medics = countAliveRole("Medic");
        return nightKills.size() >= werewolves
            && nightPeeks.size() >= seers
            && nightHeals.size() >= medics;
    }

    private boolean votesComplete() {
        return session.getCurrentVotes().size() >= session.getAlivePlayers().size();
    }

    private void resetNightState() {
        nightKills.clear();
        nightHeals.clear();
        nightPeeks.clear();
    }

    private void schedulePhaseTimer(Runnable task, long delayMs) {
        phaseTimer = scheduler.schedule(task, delayMs, TimeUnit.MILLISECONDS);
    }

    private void stopPhaseTimer() {
        if (phaseTimer != null && !phaseTimer.isDone()) {
            phaseTimer.cancel(false);
            phaseTimer = null;
        }
    }

    private Map<String, Integer> tallyVotes(Map<String, String> votes) {
        Map<String, Integer> tally = new HashMap<>();
        for (String targetId : votes.values()) {
            tally.merge(targetId, 1, Integer::sum);
        }
        return tally;
    }

    private List<String> resolveTopTargets(Map<String, Integer> tally) {
        int max = tally.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (max == 0) {
            return List.of();
        }
        List<String> topTargets = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tally.entrySet()) {
            if (entry.getValue() == max) {
                topTargets.add(entry.getKey());
            }
        }
        return topTargets;
    }

    private void assignRoles() {
        List<Player> players = new ArrayList<>(session.getAlivePlayers());
        Collections.shuffle(players, random);

        int total = players.size();
        int werewolfCount = Math.max(1, total / 4);
        boolean includeSeer = total >= 4;
        boolean includeMedic = total >= 5;

        List<PlayerRole> roles = new ArrayList<>();
        for (int i = 0; i < werewolfCount; i++) {
            roles.add(new WerewolfRole());
        }
        if (includeSeer) {
            roles.add(new SeerRole());
        }
        if (includeMedic) {
            roles.add(new MedicRole());
        }
        while (roles.size() < total) {
            roles.add(new VillagerRole());
        }
        Collections.shuffle(roles, random);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            PlayerRole role = roles.get(i);
            player.setRole(role);
            sendPrivateRole(player, role);
        }
    }

    private void sendPrivateRole(Player player, PlayerRole role) {
        GameStateUpdate update = new GameStateUpdate(
            "Your role is: " + role.getName(),
            session.getCurrentPhase(),
            session.getAlivePlayers()
        );
        update.addMetadata("role", role.getName());
        sendPrivateUpdate(player.getId(), update);
    }

    private boolean checkWinConditions() {
        long aliveWerewolves = session.countAliveWerewolves();
        long aliveVillagers = session.countAliveVillagers();

        if (aliveWerewolves == 0) {
            endGame("Villagers win!");
            return true;
        }
        if (aliveWerewolves >= aliveVillagers) {
            endGame("Werewolves win!");
            return true;
        }
        return false;
    }

    private void endGame(String message) {
        stopPhaseTimer();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("winner", message);
        Map<String, String> roles = new HashMap<>();
        for (Player player : session.getPlayers()) {
            String roleName = player.getRole() != null ? player.getRole().getName() : "Unknown";
            roles.put(player.getUsername(), roleName);
        }
        metadata.put("roles", roles);

        session.updatePhase(GameState.GAME_OVER, message, metadata);
    }

    private long countAliveRole(String roleName) {
        return session.getAlivePlayers().stream()
            .filter(p -> p.getRole() != null && roleName.equals(p.getRole().getName()))
            .count();
    }
}
