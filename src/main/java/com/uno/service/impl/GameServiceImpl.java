package com.uno.service.impl;

import com.uno.dtos.*;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.entity.Card;
import com.uno.entity.Game;
import com.uno.entity.Leaderboard;
import com.uno.entity.User;
import com.uno.repository.CardRepository;
import com.uno.repository.GameRepository;
import com.uno.repository.LeaderboardRepository;
import com.uno.repository.UserRepository;
import com.uno.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final LeaderboardRepository leaderboardRepository;

    // Thread-safe storage for active game states
    private final Map<String, GameState> gameStates = new ConcurrentHashMap<>();

    public GameServiceImpl(GameRepository gameRepository, CardRepository cardRepository,
                           UserRepository userRepository, LeaderboardRepository leaderboardRepository) {
        this.gameRepository = gameRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.leaderboardRepository = leaderboardRepository;
    }

    @Override
    public ResponseEntity<?> startGame(GameRequestDTO gameRequestDTO) {
        // Enhanced validation
        if (gameRequestDTO == null || gameRequestDTO.getGameType() == null) {
            return ResponseEntity.badRequest()
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.BAD_REQUEST, "Invalid game request"), null));
        }

        if (gameRequestDTO.getStatus() == Game.GameStatus.COMPLETED) {
            return ResponseEntity.badRequest()
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.BAD_REQUEST, "Cannot start a completed game"), null));
        }

        try {
            Game game = new Game();
            game.setStatus(Game.GameStatus.PENDING);
            game.setStartDate(LocalDateTime.now());
            game.setGameType(gameRequestDTO.getGameType());
            game.setIsMultiplayer(gameRequestDTO.getMultiplayer());

            Game savedGame = gameRepository.save(game);

            return ResponseEntity.ok(new GeneralResponseWithData<>(
                    new Status(HttpStatus.OK, "Game started successfully"), savedGame.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start game"), null));
        }
    }

    @Override
    public ResponseEntity<?> getGameStatus(Long id) {
        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.BAD_REQUEST, "Game ID cannot be null"), null));
        }

        try {
            Game game = gameRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));

            return ResponseEntity.ok(new GeneralResponseWithData<>(
                    new Status(HttpStatus.OK, "Game status retrieved successfully"), game.getStatus()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.NOT_FOUND, e.getMessage()), null));
        }
    }

    @Override
    public ResponseEntity<?> getGameHistory() {
        try {
            List<Game> completedGames = gameRepository.findByStatusOrderByEndDateDesc(Game.GameStatus.COMPLETED);

            return ResponseEntity.ok(new GeneralResponseWithData<>(
                    new Status(HttpStatus.OK, "Game history retrieved successfully"), completedGames));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve game history"), null));
        }
    }

    @Override
    public ResponseEntity<?> getGameStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalGames", gameRepository.count());
            statistics.put("completedGames", gameRepository.countByStatus(Game.GameStatus.COMPLETED));
            statistics.put("activeGames", gameRepository.countByStatus(Game.GameStatus.PLAYING));
            statistics.put("pendingGames", gameRepository.countByStatus(Game.GameStatus.PENDING));

            return ResponseEntity.ok(new GeneralResponseWithData<>(
                    new Status(HttpStatus.OK, "Game statistics retrieved successfully"), statistics));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve statistics"), null));
        }
    }

    @Override
    public ResponseEntity<?> getTopCard(Long id) {
        if (id == null) {
            return ResponseEntity.badRequest()
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.BAD_REQUEST, "Game ID cannot be null"), null));
        }

        try {
            Game game = gameRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));

            return ResponseEntity.ok(new GeneralResponseWithData<>(
                    new Status(HttpStatus.OK, "Top card retrieved successfully"), game.getTopCard()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.NOT_FOUND, e.getMessage()), null));
        }
    }

    @Override
    public ResponseEntity<?> updateTopCard(Long gameId, Integer topCardId) {
        if (gameId == null || topCardId == null) {
            return ResponseEntity.badRequest()
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.BAD_REQUEST, "Game ID and Top Card ID cannot be null"), null));
        }

        try {
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));
            Card topCard = cardRepository.findById(topCardId)
                    .orElseThrow(() -> new IllegalArgumentException("Top card not found"));

            game.setTopCard(topCard);
            gameRepository.save(game);

            return ResponseEntity.ok(new GeneralResponseWithData<>(
                    new Status(HttpStatus.OK, "Top card updated successfully"), game.getTopCard()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.NOT_FOUND, e.getMessage()), null));
        }
    }

    @Override
    public ResponseEntity<?> updateGameStatus(Long id, String status) {
        if (id == null || status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.BAD_REQUEST, "Game ID and status cannot be null or empty"), null));
        }

        try {
            Game game = gameRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));

            Game.GameStatus gameStatus;
            switch (status.toUpperCase()) {
                case "COMPLETED":
                    gameStatus = Game.GameStatus.COMPLETED;
                    game.setEndDate(LocalDateTime.now());
                    break;
                case "PLAYING":
                    gameStatus = Game.GameStatus.PLAYING;
                    break;
                case "PENDING":
                    gameStatus = Game.GameStatus.PENDING;
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(new GeneralResponseWithData<>(
                                    new Status(HttpStatus.BAD_REQUEST, "Invalid status: " + status), null));
            }

            game.setStatus(gameStatus);
            gameRepository.save(game);

            return ResponseEntity.ok(new GeneralResponseWithData<>(
                    new Status(HttpStatus.OK, "Game status updated successfully"), game.getStatus()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.NOT_FOUND, e.getMessage()), null));
        }
    }

    @Override
    public ResponseEntity<?> endGame(GameRequestDTO gameRequestDTO) {
        if (gameRequestDTO == null || gameRequestDTO.getId() == null) {
            return ResponseEntity.badRequest()
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.BAD_REQUEST, "Invalid game request"), null));
        }

        try {
            Game game = gameRepository.findById(gameRequestDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));

            // Update game status and end date
            game.setStatus(Game.GameStatus.COMPLETED);
            game.setEndDate(LocalDateTime.now());
            game.setTopCard(null);

            // Set winner if provided
            if (gameRequestDTO.getWinnerUsername() != null && !gameRequestDTO.getWinnerUsername().trim().isEmpty()) {
                User winner = userRepository.findByUsername(gameRequestDTO.getWinnerUsername())
                        .orElseThrow(() -> new IllegalArgumentException("Winner user not found"));
                game.setWinner(winner);
            }

            gameRepository.save(game);

            // Create leaderboard entry if there's a winner
            String winnerUsername = null;
            if (game.getWinner() != null) {
                Leaderboard leaderboard = new Leaderboard();
                leaderboard.setGame(game);
                leaderboard.setUser(game.getWinner());
                leaderboard.setScore(1L);
                leaderboard.setScoreDate(LocalDateTime.now());
                leaderboardRepository.save(leaderboard);
                winnerUsername = game.getWinner().getUsername();
            }

            // Clean up game state from memory
            gameStates.remove(gameRequestDTO.getId().toString());

            return ResponseEntity.ok(new GeneralResponseWithData<>(
                    new Status(HttpStatus.OK, "Game ended successfully"), winnerUsername));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GeneralResponseWithData<>(
                            new Status(HttpStatus.NOT_FOUND, e.getMessage()), null));
        }
    }

    // Real-time game state management methods
    public synchronized GameStateMessage processMove(String gameId, PlayCardMessage move) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        if (gameState.getStatus() == GameStatus.FINISHED) {
            throw new IllegalStateException("Game has already finished");
        }

        PlayerState currentPlayer = gameState.getPlayers().get(gameState.getCurrentPlayerIndex());
        if (!currentPlayer.getPlayerId().equals(move.getPlayerId())) {
            throw new IllegalStateException("Not your turn");
        }

        // Validate move legality
        if (!isValidMove(gameState, move.getCardPlayed())) {
            throw new IllegalArgumentException("Invalid move");
        }

        // Update game state with the move
        updateGameStateWithMove(gameState, move);

        // Check for win condition
        if (playerHasWon(currentPlayer)) {
            gameState.setStatus(GameStatus.FINISHED);
            gameState.setWinnerPlayerId(currentPlayer.getPlayerId());
        }

        return new GameStateMessage(gameState.getGameId(),
                gameState.getPlayers(),
                gameState.getTopCard(),
                gameState.getStatus(),
                gameState.getWinnerPlayerId(),
                gameState.getCurrentPlayerIndex(),
                gameState.getTurnDirection(),
                gameState.getDrawPenaltyCount(),
        gameState.getDrawPile().size());
    }

    @Override
    public GameStateMessage getCurrentState(String gameId) {
        GameState gameState = gameStates.get(gameId);
        if (gameState == null) {
            throw new IllegalArgumentException("Game with ID " + gameId + " not found");
        }
        return toGameStateMessage(gameState);
    }

    private GameStateMessage toGameStateMessage(GameState gameState) {
        return new GameStateMessage(
                gameState.getGameId(),
                gameState.getPlayers(),
                gameState.getTopCard(),
                gameState.getStatus(),
                gameState.getWinnerPlayerId(),
                gameState.getCurrentPlayerIndex(),
                gameState.getTurnDirection(),
                gameState.getDrawPenaltyCount(),
                gameState.getDrawPileSize()  // Now matches the constructor parameter count
        );
    }

    private boolean isValidMove(GameState gameState, Card cardPlayed) {
        Card topCard = gameState.getTopCard();

        if (topCard == null) return true; // First card can be anything

        // Wild cards can always be played
        if (cardPlayed.getCardType() == Card.CardType.WILD ||
                cardPlayed.getCardType() == Card.CardType.WILD_DRAW_FOUR) {
            return true;
        }

        // Color matches
        if (cardPlayed.getColor() == topCard.getColor()) {
            return true;
        }

        // Number matches (for number cards)
        if (cardPlayed.getCardType() == Card.CardType.NUMBER &&
                topCard.getCardType() == Card.CardType.NUMBER &&
                Objects.equals(cardPlayed.getNumber(), topCard.getNumber())) {
            return true;
        }

        // Action card type matches
        if (cardPlayed.getCardType() == topCard.getCardType() &&
                cardPlayed.getCardType() != Card.CardType.NUMBER) {
            return true;
        }

        return false;
    }

    private void updateGameStateWithMove(GameState gameState, PlayCardMessage move) {
        Card playedCard = move.getCardPlayed();
        int currentPlayerIndex = gameState.getCurrentPlayerIndex();
        PlayerState currentPlayer = gameState.getPlayers().get(currentPlayerIndex);

        // Remove played card from player's hand
        boolean removed = currentPlayer.getHand().removeIf(card ->
                card.getCardType() == playedCard.getCardType() &&
                        card.getColor() == playedCard.getColor() &&
                        Objects.equals(card.getNumber(), playedCard.getNumber()));

        if (!removed) {
            throw new IllegalStateException("Player does not have the played card");
        }

        // Update top card
        gameState.setTopCard(playedCard);

        // Handle UNO declaration
        if (move.isDeclaredUNO()) {
            currentPlayer.setHasDeclaredUNO(true);
        } else if (currentPlayer.getHand().size() == 1) {
            currentPlayer.setHasDeclaredUNO(false);
            // Apply penalty if needed (draw 2 cards for not declaring UNO)
            drawCardsToPlayer(gameState, currentPlayer, 2);
        }

        // Apply special card effects
        applyCardEffect(gameState, playedCard);

        // Check win condition
        if (currentPlayer.getHand().isEmpty()) {
            gameState.setStatus(GameStatus.FINISHED);
            gameState.setWinnerPlayerId(currentPlayer.getPlayerId());
            return;
        }

        // Advance to next turn
        advanceTurn(gameState);
    }

    private void applyCardEffect(GameState gameState, Card card) {
        switch (card.getCardType()) {
            case SKIP:
                // Skip will be handled in advanceTurn by advancing twice
                gameState.setSkipNextPlayer(true);
                break;

            case REVERSE:
                gameState.setTurnDirection(
                        gameState.getTurnDirection() == TurnDirection.CLOCKWISE ?
                                TurnDirection.COUNTERCLOCKWISE : TurnDirection.CLOCKWISE);
                break;

            case DRAW_TWO:
                gameState.setDrawPenaltyCount(2);
                break;

            case WILD_DRAW_FOUR:
                gameState.setDrawPenaltyCount(4);
                break;

            case WILD:
                // Color choice should be handled in the PlayCardMessage
                break;

            case NUMBER:
            default:
                gameState.setDrawPenaltyCount(0);
                break;
        }
    }

    private void advanceTurn(GameState gameState) {
        int playerCount = gameState.getPlayers().size();
        int currentIndex = gameState.getCurrentPlayerIndex();
        int nextIndex = calculateNextPlayerIndex(gameState, currentIndex, playerCount);

        // Apply draw penalty if exists
        if (gameState.getDrawPenaltyCount() > 0) {
            PlayerState nextPlayer = gameState.getPlayers().get(nextIndex);
            drawCardsToPlayer(gameState, nextPlayer, gameState.getDrawPenaltyCount());
            gameState.setDrawPenaltyCount(0);

            // Skip the penalized player's turn
            nextIndex = calculateNextPlayerIndex(gameState, nextIndex, playerCount);
        }

        // Handle skip effect
        if (gameState.isSkipNextPlayer()) {
            nextIndex = calculateNextPlayerIndex(gameState, nextIndex, playerCount);
            gameState.setSkipNextPlayer(false);
        }

        gameState.setCurrentPlayerIndex(nextIndex);
    }

    private int calculateNextPlayerIndex(GameState gameState, int currentIndex, int playerCount) {
        if (gameState.getTurnDirection() == TurnDirection.CLOCKWISE) {
            return (currentIndex + 1) % playerCount;
        } else {
            return (currentIndex - 1 + playerCount) % playerCount;
        }
    }

    private void drawCardsToPlayer(GameState gameState, PlayerState player, int count) {
        for (int i = 0; i < count; i++) {
            if (gameState.getDrawPile().isEmpty()) {
                reshuffleDrawPile(gameState);
            }

            if (!gameState.getDrawPile().isEmpty()) {
                Card card = gameState.getDrawPile().remove(0);
                player.getHand().add(card);
            } else {
                // No more cards available
                break;
            }
        }
    }

    private void reshuffleDrawPile(GameState gameState) {
        List<Card> discardPile = gameState.getDiscardPile();
        List<Card> drawPile = gameState.getDrawPile();

        if (discardPile.size() <= 1) {
            return; // Can't reshuffle if only top card remains
        }

        Card topCard = gameState.getTopCard();

        // Move all cards except top card from discard to draw pile
        discardPile.remove(topCard);
        Collections.shuffle(discardPile);

        drawPile.addAll(discardPile);
        discardPile.clear();
        discardPile.add(topCard);
    }

    private boolean playerHasWon(PlayerState player) {
        return player.getHand().isEmpty();
    }

    // Utility method to convert DTO to entity
    public Game toEntity(GameRequestDTO gameDTO) {
        Game game = new Game();
        game.setStatus(gameDTO.getStatus());
        game.setStartDate(LocalDateTime.now());
        game.setEndDate(gameDTO.getEndDate());
        game.setGameType(gameDTO.getGameType());

        if (gameDTO.getTopCardId() != null) {
            Card card = cardRepository.findById(gameDTO.getTopCardId())
                    .orElseThrow(() -> new IllegalArgumentException("Card not found"));
            game.setTopCard(card);
        }

        game.setIsMultiplayer(gameDTO.getMultiplayer());

        if (gameDTO.getWinnerUsername() != null && !gameDTO.getWinnerUsername().trim().isEmpty()) {
            User user = userRepository.findByUsername(gameDTO.getWinnerUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            game.setWinner(user);
        }

        return game;
    }

    // Method to initialize a new game state in memory
    public GameState initializeGameState(String gameId, List<String> playerIds) {
        GameState gameState = new GameState();
        gameState.setGameId(gameId);
        gameState.setStatus(GameStatus.WAITING_FOR_PLAYERS);
        gameState.setCurrentPlayerIndex(0);
        gameState.setTurnDirection(TurnDirection.CLOCKWISE);
        gameState.setDrawPenaltyCount(0);

        // Initialize players
        List<PlayerState> players = new ArrayList<>();
        for (String playerId : playerIds) {
            PlayerState player = new PlayerState();
            player.setPlayerId(playerId);
            player.setHand(new ArrayList<>());
            player.setHasDeclaredUNO(false);
            players.add(player);
        }
        gameState.setPlayers(players);

        // Initialize draw pile with shuffled deck
        gameState.setDrawPile(createShuffledDeck());
        gameState.setDiscardPile(new ArrayList<>());

        // Deal initial hands (7 cards per player)
        for (PlayerState player : players) {
            drawCardsToPlayer(gameState, player, 7);
        }

        // Set initial top card
        if (!gameState.getDrawPile().isEmpty()) {
            Card topCard = gameState.getDrawPile().remove(0);
            gameState.setTopCard(topCard);
            gameState.getDiscardPile().add(topCard);
        }

        gameStates.put(gameId, gameState);
        return gameState;
    }

    private List<Card> createShuffledDeck() {
        List<Card> deck = new ArrayList<>();
        // This would typically create a full UNO deck
        // Implementation depends on your Card entity structure

        // Example deck creation - adjust based on your Card entity
        // Colors: RED, BLUE, GREEN, YELLOW
        // Numbers: 0-9 (0 appears once, 1-9 appear twice per color)
        // Action cards: SKIP, REVERSE, DRAW_TWO (2 per color)
        // Wild cards: WILD (4 cards), WILD_DRAW_FOUR (4 cards)

        Collections.shuffle(deck);
        return deck;
    }
}