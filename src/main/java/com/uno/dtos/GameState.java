package com.uno.dtos;

import com.uno.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameState {

    private String gameId;                    // Unique identifier for this game session

    private GameStatus status;                // Current status of the game (WAITING_FOR_PLAYERS, ONGOING, FINISHED)

    private List<PlayerState> players;       // List of players and their current info (hand, score, etc.)

    private int currentPlayerIndex;           // Index of player whose turn it is

    private Card topCard;                    // Card currently on the discard pile

    private List<Card> drawPile;             // Cards left to draw from

    private List<Card> discardPile;          // Cards that have been played (for reshuffling)

    private TurnDirection turnDirection;      // Current turn direction (CLOCKWISE, COUNTERCLOCKWISE)

    private int drawPenaltyCount;            // Number of cards to draw due to penalties (Draw Two, Wild Draw Four)

    private String winnerPlayerId;           // ID of the winner, null if none yet

    private boolean skipNextPlayer;          // Flag to skip the next player's turn (used for SKIP cards)

    private String chosenColor;              // Color chosen when a Wild card is played (RED, BLUE, GREEN, YELLOW)

    private LocalDateTime gameStartTime;     // When the game started

    private LocalDateTime lastMoveTime;      // When the last move was made (for timeout handling)

    private int roundNumber;                 // Current round number (if playing multiple rounds)

    private Map<String, Integer> playerScores; // Player scores across multiple rounds

    // Game settings/configuration
    private boolean allowStackingDrawCards;   // Whether Draw Two cards can be stacked
    private boolean jumpInAllowed;           // Whether players can jump in with identical cards
    private int maxHandSize;                 // Maximum hand size before auto-win (house rules)
    private int timeoutSeconds;              // Time limit per turn in seconds

    // Statistics for this game session
    private int totalCardsPlayed;            // Total number of cards played in this game
    private Map<String, Integer> cardsPlayedByPlayer; // Cards played by each player

    /**
     * Checks if the game is in a finished state
     */
    public boolean isGameFinished() {
        return status == GameStatus.FINISHED;
    }

    /**
     * Gets the current player
     */
    public PlayerState getCurrentPlayer() {
        if (players == null || players.isEmpty() || currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    /**
     * Gets a player by their ID
     */
    public PlayerState getPlayerById(String playerId) {
        if (players == null || playerId == null) {
            return null;
        }
        return players.stream().filter(player -> playerId.equals(player.getPlayerId())).findFirst().orElse(null);
    }

    /**
     * Checks if all players are ready to start
     */
    public boolean areAllPlayersReady() {
        if (players == null || players.isEmpty()) {
            return false;
        }
        return players.stream().allMatch(PlayerState::isActive);
    }

    /**
     * Gets the number of cards remaining in draw pile
     */
    public int getDrawPileSize() {
        return drawPile != null ? drawPile.size() : 0;
    }

    /**
     * Gets the number of cards in discard pile
     */
    public int getDiscardPileSize() {
        return discardPile != null ? discardPile.size() : 0;
    }

    /**
     * Checks if a reshuffle is needed (draw pile is empty)
     */
    public boolean needsReshuffle() {
        return getDrawPileSize() == 0 && getDiscardPileSize() > 1;
    }

    /**
     * Gets the effective color for card matching
     * (considers chosen color from Wild cards)
     */
    public String getEffectiveColor() {
        if (chosenColor != null && !chosenColor.isEmpty()) {
            return chosenColor;
        }
        return topCard != null ? topCard.getColor().toString() : null;
    }
}
