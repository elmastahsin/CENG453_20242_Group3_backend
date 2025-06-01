package com.uno.dtos;

import com.uno.entity.Card;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStateMessage {

    private Long gameId;
    private List<PlayerInfo> players;
    private String currentTurnPlayerId;
    private Card topCard;
    private int drawPileCount;
    private SpecialCardsInPlay specialCardsInPlay;
    private TurnDirection turnDirection;
    private String winnerPlayerId;
    private int drawPenaltyCount;
    private String chosenColor; // For Wild cards
    private GameStatus status;

    // Constructor that matches your current usage pattern
    public GameStateMessage(String gameId, List<PlayerState> players, Card topCard,
                            GameStatus status, String winnerPlayerId, int currentPlayerIndex,
                            TurnDirection turnDirection, int drawPenaltyCount, int drawPileCount) {
        this.gameId = Long.parseLong(gameId);
        this.players = players.stream()
                .map(player -> new PlayerInfo(
                        player.getPlayerId(),
                        player.getUsername(),
                        player.getHand(),
                        player.getScore(),
                        player.isHasDeclaredUNO()
                ))
                .collect(Collectors.toList());
        this.currentTurnPlayerId = players.get(currentPlayerIndex).getPlayerId();
        this.topCard = topCard;
        this.drawPileCount = drawPileCount;
        this.specialCardsInPlay = determineSpecialCardsInPlay(topCard, drawPenaltyCount);
        this.turnDirection = turnDirection;
        this.winnerPlayerId = winnerPlayerId;
        this.drawPenaltyCount = drawPenaltyCount;
        this.status = status;
    }

    // Alternative constructor using GameState object (cleaner approach)
    public GameStateMessage(GameState gameState) {
        this.gameId = Long.parseLong(gameState.getGameId());
        this.players = gameState.getPlayers().stream()
                .map(player -> new PlayerInfo(
                        player.getPlayerId(),
                        player.getUsername(),
                        player.getHand(),
                        player.getScore(),
                        player.isHasDeclaredUNO()
                ))
                .collect(Collectors.toList());

        // Safe way to get current player ID
        PlayerState currentPlayer = gameState.getCurrentPlayer();
        this.currentTurnPlayerId = currentPlayer != null ? currentPlayer.getPlayerId() : null;

        this.topCard = gameState.getTopCard();
        this.drawPileCount = gameState.getDrawPileSize();
        this.specialCardsInPlay = determineSpecialCardsInPlay(gameState.getTopCard(), gameState.getDrawPenaltyCount());
        this.turnDirection = gameState.getTurnDirection();
        this.winnerPlayerId = gameState.getWinnerPlayerId();
        this.drawPenaltyCount = gameState.getDrawPenaltyCount();
        this.chosenColor = gameState.getChosenColor();
        this.status = gameState.getStatus();
    }

    // Helper method to determine special cards in play
    private SpecialCardsInPlay determineSpecialCardsInPlay(Card topCard, int drawPenaltyCount) {
        if (topCard == null) {
            return new SpecialCardsInPlay(false, false);
        }

        boolean hasDrawPenalty = drawPenaltyCount > 0;
        boolean hasColorChoice = topCard.getCardType() == Card.CardType.WILD ||
                topCard.getCardType() == Card.CardType.WILD_DRAW_FOUR;

        return new SpecialCardsInPlay(hasDrawPenalty, hasColorChoice);
    }

    // Convenience method to check if it's a specific player's turn
    public boolean isPlayerTurn(String playerId) {
        return currentTurnPlayerId != null && currentTurnPlayerId.equals(playerId);
    }

    // Get player info by ID
    public PlayerInfo getPlayerInfo(String playerId) {
        return players.stream()
                .filter(p -> playerId.equals(p.getPlayerId()))
                .findFirst()
                .orElse(null);
    }

    // Check if game is finished
    public boolean isGameFinished() {
        return status == GameStatus.FINISHED;
    }
}


