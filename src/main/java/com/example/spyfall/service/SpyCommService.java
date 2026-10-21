package com.example.spyfall.service;

import com.example.spyfall.common.Spy2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SpyCommService {

    public static final String GAME_SPY = "spy";
    public static final String GAME_SPY2 = "spy2";

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, GameState> games = new HashMap<>();
    private final Map<String, SessionRef> sessionRefs = new HashMap<>();

    public SpyCommService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        games.put(GAME_SPY, new GameState());
        games.put(GAME_SPY2, new GameState());
    }

    public synchronized void resetGame(String game) {
        GameState state = stateOf(game);
        state.playersById.clear();
        state.deviceToPlayerId.clear();
        state.restrictEliminated = true;
        broadcastState(game);
    }

    public synchronized void upsertPlayer(String game, Spy2 player) {
        if (player == null || !StringUtils.hasText(player.getIpConfig())) {
            return;
        }
        GameState state = stateOf(game);
        PlayerState value = new PlayerState(
                player.getId(),
                player.getIpConfig(),
                StringUtils.hasText(player.getUserName()) ? player.getUserName() : ("Người chơi " + player.getId()),
                !player.isRemove()
        );
        state.playersById.put(player.getId(), value);
        state.deviceToPlayerId.put(value.deviceId, value.playerId);
        broadcastState(game);
    }

    public synchronized void markRemoved(String game, int playerId) {
        GameState state = stateOf(game);
        PlayerState player = state.playersById.get(playerId);
        if (player != null && player.alive) {
            player.alive = false;
            broadcastState(game);
        }
    }

    public synchronized Map<String, Object> registerSocket(String game, String deviceId, String sessionId, String displayName) {
        if (!StringUtils.hasText(game) || !StringUtils.hasText(deviceId) || !StringUtils.hasText(sessionId)) {
            return Map.of("ok", false, "message", "Thông tin đăng ký socket không hợp lệ");
        }
        GameState state = stateOf(game);
        state.deviceToSessions.computeIfAbsent(deviceId, k -> new LinkedHashSet<>()).add(sessionId);
        PlayerState assignedPlayer = playerOf(state, deviceId);
        if (assignedPlayer != null && StringUtils.hasText(assignedPlayer.userName)) {
            state.lobbyNames.put(deviceId, assignedPlayer.userName);
        } else if (StringUtils.hasText(displayName)) {
            state.lobbyNames.put(deviceId, displayName.trim());
        } else if (!state.lobbyNames.containsKey(deviceId)) {
            state.lobbyNames.put(deviceId, "Người chơi");
        }
        sessionRefs.put(sessionId, new SessionRef(game, deviceId));
        Map<String, Object> payload = statePayload(game, deviceId);
        sendToDevice(game, deviceId, payload);
        broadcastState(game);
        return Map.of("ok", true);
    }

    public synchronized void unregisterSocket(String sessionId) {
        SessionRef ref = sessionRefs.remove(sessionId);
        if (ref == null) {
            return;
        }
        GameState state = games.get(ref.game);
        if (state == null) {
            return;
        }
        Set<String> sessions = state.deviceToSessions.get(ref.deviceId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                state.deviceToSessions.remove(ref.deviceId);
                state.lobbyNames.remove(ref.deviceId);
            }
        }
        broadcastState(ref.game);
    }

    public synchronized Map<String, Object> getPlayerState(String game, String deviceId) {
        return statePayload(game, deviceId);
    }

    public synchronized Map<String, Object> getAdminState(String game) {
        GameState state = stateOf(game);
        List<Map<String, Object>> players = new ArrayList<>();
        for (PlayerState player : state.playersById.values()) {
            ParticipantState participant = new ParticipantState(
                    player.playerId,
                    player.deviceId,
                    player.userName,
                    player.alive,
                    true,
                    state.deviceToSessions.containsKey(player.deviceId)
            );
            players.add(playerView(state, null, participant));
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("game", game);
        payload.put("restrictEliminated", state.restrictEliminated);
        payload.put("players", players);
        return payload;
    }

    public synchronized Map<String, Object> setRestrictEliminated(String game, boolean enabled) {
        GameState state = stateOf(game);
        state.restrictEliminated = enabled;
        broadcastState(game);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("restrictEliminated", state.restrictEliminated);
        return payload;
    }

    public synchronized Map<String, Object> sendChat(String game, String fromDeviceId, String channel, String message) {
        GameState state = stateOf(game);
        if (!StringUtils.hasText(message)) {
            return Map.of("ok", false, "message", "Tin nhắn trống");
        }
        ParticipantState sender = participantOf(state, fromDeviceId);
        if (sender == null) {
            return Map.of("ok", false, "message", "Người chơi không hợp lệ");
        }
        String normalizedChannel = normalizeChannel(state, sender, channel);
        if (!canSendChannel(state, sender, normalizedChannel)) {
            return Map.of("ok", false, "message", "Bạn không có quyền chat vào kênh này");
        }

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "chat");
        event.put("channel", normalizedChannel);
        event.put("senderDeviceId", sender.deviceId);
        event.put("senderPlayerId", sender.playerId != null ? sender.playerId : -1);
        event.put("senderName", sender.userName);
        event.put("message", message.trim());
        event.put("timestamp", System.currentTimeMillis());

        for (ParticipantState receiver : participantsOf(state).values()) {
            if (receiver.online && canReceiveChannel(state, receiver, normalizedChannel)) {
                sendToDevice(game, receiver.deviceId, event);
            }
        }
        return Map.of("ok", true);
    }

    public synchronized Map<String, Object> relaySignal(String game, String fromDeviceId, String targetDeviceId,
                                                        String signalType, Object payload) {
        GameState state = stateOf(game);
        ParticipantState from = participantOf(state, fromDeviceId);
        ParticipantState target = participantOf(state, targetDeviceId);
        if (from == null || target == null) {
            return Map.of("ok", false, "message", "Người chơi không hợp lệ");
        }
        boolean anyDirectionAllowed = canHear(state, from, target) || canHear(state, target, from);
        if (!anyDirectionAllowed) {
            return Map.of("ok", false, "message", "Kết nối voice không được phép");
        }
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "signal");
        event.put("fromDeviceId", fromDeviceId);
        event.put("signalType", signalType);
        event.put("payload", payload);
        sendToDevice(game, targetDeviceId, event);
        return Map.of("ok", true);
    }

    private Map<String, Object> statePayload(String game, String deviceId) {
        GameState state = stateOf(game);
        ParticipantState me = participantOf(state, deviceId);
        List<Map<String, Object>> participants = new ArrayList<>();
        for (ParticipantState participant : participantsOf(state).values()) {
            participants.add(playerView(state, me, participant));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "state");
        payload.put("ok", true);
        payload.put("game", game);
        payload.put("deviceId", deviceId);
        payload.put("restrictEliminated", state.restrictEliminated);
        payload.put("isEliminated", me != null && !me.alive);
        payload.put("channels", channelsOf(state, me));
        payload.put("participants", participants);
        payload.put("message", me == null ? "Đang ở phòng chờ" : "Đã vào phòng");
        return payload;
    }

    private List<Map<String, Object>> channelsOf(GameState state, ParticipantState me) {
        if (me == null) {
            return List.of(channelView("public", "Kênh công khai", true));
        }
        List<Map<String, Object>> channels = new ArrayList<>();
        boolean isRestrictedDead = me.assigned && state.restrictEliminated && !me.alive;
        channels.add(channelView("public", "Kênh công khai", !isRestrictedDead));
        if (isRestrictedDead) {
            channels.add(channelView("dead", "Kênh đã bị loại", true));
        }
        return channels;
    }

    private Map<String, Object> channelView(String key, String title, boolean canSend) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("key", key);
        item.put("title", title);
        item.put("canSend", canSend);
        item.put("readOnly", !canSend);
        return item;
    }

    private Map<String, Object> playerView(GameState state, ParticipantState me, ParticipantState player) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("deviceId", player.deviceId);
        item.put("playerId", player.playerId != null ? player.playerId : -1);
        item.put("userName", player.userName);
        item.put("alive", player.alive);
        item.put("online", player.online);
        if (me != null) {
            item.put("canHear", canHear(state, player, me));
            item.put("canSend", canHear(state, me, player));
        } else {
            item.put("canHear", false);
            item.put("canSend", false);
        }
        return item;
    }

    private boolean canHear(GameState state, ParticipantState speaker, ParticipantState listener) {
        if (!speaker.assigned || !listener.assigned) {
            return true;
        }
        if (!state.restrictEliminated) {
            return true;
        }
        if (speaker.alive) {
            return true;
        }
        return !listener.alive;
    }

    private String normalizeChannel(GameState state, ParticipantState sender, String channel) {
        if (!sender.assigned) {
            return "public";
        }
        if (!state.restrictEliminated) {
            return "public";
        }
        if (sender.alive) {
            return "public";
        }
        return "dead";
    }

    private boolean canSendChannel(GameState state, ParticipantState sender, String channel) {
        if (!sender.assigned) {
            return "public".equals(channel);
        }
        if ("public".equals(channel)) {
            return sender.alive || !state.restrictEliminated;
        }
        if ("dead".equals(channel)) {
            return state.restrictEliminated && !sender.alive;
        }
        return false;
    }

    private boolean canReceiveChannel(GameState state, ParticipantState receiver, String channel) {
        if (!receiver.assigned) {
            return "public".equals(channel);
        }
        if ("public".equals(channel)) {
            return true;
        }
        if ("dead".equals(channel)) {
            return state.restrictEliminated && !receiver.alive;
        }
        return false;
    }

    private ParticipantState participantOf(GameState state, String deviceId) {
        return participantsOf(state).get(deviceId);
    }

    private Map<String, ParticipantState> participantsOf(GameState state) {
        Map<String, ParticipantState> participants = new LinkedHashMap<>();
        for (PlayerState player : state.playersById.values()) {
            participants.put(player.deviceId, new ParticipantState(
                    player.playerId,
                    player.deviceId,
                    player.userName,
                    player.alive,
                    true,
                    state.deviceToSessions.containsKey(player.deviceId)
            ));
        }
        for (String deviceId : state.deviceToSessions.keySet()) {
            if (participants.containsKey(deviceId)) {
                continue;
            }
            String name = state.lobbyNames.getOrDefault(deviceId, "Khách");
            participants.put(deviceId, new ParticipantState(
                    null,
                    deviceId,
                    name,
                    true,
                    false,
                    true
            ));
        }
        return participants;
    }

    private PlayerState playerOf(GameState state, String deviceId) {
        Integer playerId = state.deviceToPlayerId.get(deviceId);
        if (playerId == null) {
            return null;
        }
        return state.playersById.get(playerId);
    }

    private GameState stateOf(String game) {
        GameState state = games.get(game);
        if (state == null) {
            throw new IllegalArgumentException("Game không hỗ trợ: " + game);
        }
        return state;
    }

    private void broadcastState(String game) {
        GameState state = stateOf(game);
        for (String deviceId : state.deviceToSessions.keySet()) {
            sendToDevice(game, deviceId, statePayload(game, deviceId));
        }
    }

    private void sendToDevice(String game, String deviceId, Map<String, Object> payload) {
        Set<String> sessions = stateOf(game).deviceToSessions.get(deviceId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        messagingTemplate.convertAndSend("/topic/comm/" + game + "/" + deviceId, payload);
    }

    private static class GameState {
        private final Map<Integer, PlayerState> playersById = new LinkedHashMap<>();
        private final Map<String, Integer> deviceToPlayerId = new LinkedHashMap<>();
        private final Map<String, Set<String>> deviceToSessions = new LinkedHashMap<>();
        private final Map<String, String> lobbyNames = new LinkedHashMap<>();
        private boolean restrictEliminated = true;
    }

    private static class PlayerState {
        private final int playerId;
        private final String deviceId;
        private final String userName;
        private boolean alive;

        private PlayerState(int playerId, String deviceId, String userName, boolean alive) {
            this.playerId = playerId;
            this.deviceId = deviceId;
            this.userName = userName;
            this.alive = alive;
        }
    }

    private static class ParticipantState {
        private final Integer playerId;
        private final String deviceId;
        private final String userName;
        private final boolean alive;
        private final boolean assigned;
        private final boolean online;

        private ParticipantState(Integer playerId, String deviceId, String userName, boolean alive, boolean assigned, boolean online) {
            this.playerId = playerId;
            this.deviceId = deviceId;
            this.userName = userName;
            this.alive = alive;
            this.assigned = assigned;
            this.online = online;
        }
    }

    private static class SessionRef {
        private final String game;
        private final String deviceId;

        private SessionRef(String game, String deviceId) {
            this.game = game;
            this.deviceId = deviceId;
        }
    }
}
