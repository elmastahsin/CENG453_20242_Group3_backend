package com.uno.websocket;

import com.uno.entity.Game;
import com.uno.entity.GamePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSession {
    private final Game game;
    private final List<GamePlayer> players;
    private final Object lock = new Object();
    private int currentPlayerIndex = 0;
    private boolean isClockwise = true;
    // For Wild Draw Four challenge
    private boolean challengeInProgress = false;
    private String challengerUsername;
    private String challengedUsername;

    public GameSession(Game game) {
        this.game = game;
        this.players = new ArrayList<>();
    }

    public void addPlayer(GamePlayer player) {
        synchronized (lock) {
            if (players.size() < 4) {
                players.add(player);
            }
        }
    }

    public boolean isPlayerTurn(String username) {
        synchronized (lock) {
            if (currentPlayerIndex < players.size()) {
                return players.get(currentPlayerIndex).getUser().getUsername().equals(username);
            }
            return false;
        }
    }

    public void nextTurn() {
        synchronized (lock) {
            if (isClockwise) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            } else {
                currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
            }
        }
    }

    public void reverseDirection() {
        synchronized (lock) {
            isClockwise = !isClockwise;
        }
    }

    public void skipTurn() {
        synchronized (lock) {
            nextTurn();
        }
    }

    public Map<String, Object> getGameState() {
        synchronized (lock) {
            Map<String, Object> state = new HashMap<>();
            state.put("gameId", game.getGameId());
            state.put("status", game.getStatus());
            state.put("isClockwise", isClockwise);
            state.put("currentPlayer", players.get(currentPlayerIndex).getUser().getUsername());
            state.put("topCard", game.getTopCard());
            // Include other necessary game state information
            return state;
        }
    }

    // Methods for handling Wild Draw Four challenges
    public void startChallenge(String challenger, String challenged) {
        synchronized (lock) {
            challengeInProgress = true;
            challengerUsername = challenger;
            challengedUsername = challenged;
        }
    }

    public void resolveChallenge(boolean challengeSuccessful) {
        synchronized (lock) {
            challengeInProgress = false;
            // Logic for resolving the challenge
            if (challengeSuccessful) {
                // If challenge is successful, the challenged player draws cards
                GamePlayer challengedPlayer = players.stream()
                        .filter(player -> player.getUser().getUsername().equals(challengedUsername))
                        .findFirst()
                        .orElse(null);
                if (challengedPlayer != null) {
                    // Logic for drawing cards
                    // challengedPlayer.drawCards(4); // Example method to draw cards
                }
            } else {
                // If challenge fails, the challenger draws cards
                GamePlayer challengerPlayer = players.stream()
                        .filter(player -> player.getUser().getUsername().equals(challengerUsername))
                        .findFirst()
                        .orElse(null);
                if (challengerPlayer != null) {
                    // Logic for drawing cards
                    // challengerPlayer.drawCards(4); // Example method to draw cards
                }
            }
        }
    }
}