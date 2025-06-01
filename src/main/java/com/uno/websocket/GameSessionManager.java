package com.uno.websocket;

import com.uno.entity.Game;
import com.uno.repository.GamePlayerRepository;
import com.uno.repository.GameRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionManager {
    private final Map<Long, GameSession> activeSessions = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;

    public GameSessionManager(SimpMessagingTemplate messagingTemplate,
                              GameRepository gameRepository,
                              GamePlayerRepository gamePlayerRepository) {
        this.messagingTemplate = messagingTemplate;
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
    }

    public void createSession(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!activeSessions.containsKey(gameId)) {
            GameSession session = new GameSession(game);
            activeSessions.put(gameId, session);
        }
    }

    public GameSession getSession(Long gameId) {
        return activeSessions.get(gameId);
    }

    public void removeSession(Long gameId) {
        activeSessions.remove(gameId);
    }

    public void broadcastGameState(Long gameId) {
        GameSession session = getSession(gameId);
        if (session != null) {
            messagingTemplate.convertAndSend("/topic/game/" + gameId, session.getGameState());
        }
    }

    public void sendMessageToPlayer(Long gameId, String username, Object message) {
        messagingTemplate.convertAndSendToUser(username, "/queue/game/" + gameId, message);
    }

    public void logBroadcast(String topic, Object payload) {
        System.out.println("[GameSessionManager] Broadcasting to " + topic + ": " + payload);
    }

    public void broadcastToLobby(Long gameId, Object lobbyState) {
        String topic = "/topic/game/" + gameId + "/lobby";
        logBroadcast(topic, lobbyState);
        messagingTemplate.convertAndSend(topic, lobbyState);
    }

    public void broadcastToStart(Long gameId, Object startMsg) {
        String topic = "/topic/game/" + gameId + "/start";
        logBroadcast(topic, startMsg);
        messagingTemplate.convertAndSend(topic, startMsg);
    }

    public GamePlayerRepository getGamePlayerRepository() {
        return gamePlayerRepository;
    }
}
