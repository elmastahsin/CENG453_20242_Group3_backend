package com.uno.websocket;

import com.uno.entity.Card;
import com.uno.entity.Game;
import com.uno.repository.CardRepository;
import com.uno.repository.GameRepository;
import com.uno.repository.GamePlayerRepository;
import org.modelmapper.spi.ErrorMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class GameWebSocketController {
    private final GameSessionManager sessionManager;
    private final CardRepository cardRepository;
    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;

    public GameWebSocketController(GameSessionManager sessionManager, CardRepository cardRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository) {
        this.sessionManager = sessionManager;
        this.cardRepository = cardRepository;
        this.gameRepository = gameRepository;
        this.gamePlayerRepository = gamePlayerRepository;
    }

    @MessageMapping("/game/{gameId}/join")
    public void joinGame(@DestinationVariable Long gameId, Principal principal) {
        System.out.println("[WebSocket][REQUEST] /game/" + gameId + "/join from user: " + principal.getName());
        GameSession session = sessionManager.getSession(gameId);
        if (session != null) {
            // Add player to session only if not already there
            String username = principal.getName();
            var gamePlayer = gamePlayerRepository.findByGameIdAndUsername(gameId, username);
            if (gamePlayer != null) {
                boolean playerAlreadyInSession = session.getPlayers().stream()
                    .anyMatch(p -> p.getUser().getUsername().equals(username));

                if (!playerAlreadyInSession) {
                    session.addPlayer(gamePlayer);
                } else {
                    System.out.println("[WebSocketController] Player " + username + " already in session for game #" + gameId);
                }
            }

            // Broadcast lobby state to /topic/game/{gameId}/lobby
            var players = session.getPlayers().stream()
                .filter(p -> p != null && p.getUser() != null)
                .map(p -> p.getUser().getUsername())
                .distinct() // Ensure no duplicate usernames
                .toList();

            boolean ready = players.size() >= 2; // Change 2 to required player count if needed
            var lobbyState = new HashMap<String, Object>();
            lobbyState.put("gameId", gameId);
            lobbyState.put("players", players);
            lobbyState.put("ready", ready);
            sessionManager.broadcastToLobby(gameId, lobbyState);

            // If ready, initialize game and broadcast start event
            if (ready) {
                System.out.println("[WebSocketController] Game #" + gameId + " is ready with " + players.size() + " players - initializing game!");
                initializeGame(gameId, session, players);
            }
        }
    }

    /**
     * Initialize the game, set top card, deal cards to players, and broadcast game state
     */
    private void initializeGame(Long gameId, GameSession session, List<String> players) {
        try {
            // Get the game from database
            Game game = gameRepository.findById(gameId).orElseThrow(() ->
                new IllegalStateException("Game not found with ID: " + gameId));

            // 1. Initialize a deck and shuffle cards
            session.initializeGameDeck(cardRepository);

            // 2. Deal exactly 7 cards to each player (skips players who already have cards)
            for (var player : session.getPlayers()) {
                session.dealInitialCardsToPlayer(player, cardRepository);
            }

            // 3. Set the initial top card - use a playable card
            Card topCard = session.drawFirstPlayableCard(cardRepository);
            game.setTopCard(topCard);

            // 4. Update game status to PLAYING (IN_PROGRESS)
            game.setStatus(Game.GameStatus.PLAYING);
            gameRepository.save(game);

            System.out.println("[WebSocketController] Game #" + gameId + " initialized with top card: " +
                topCard.getId() + " (" + topCard.getColor() + " " +
                (topCard.getNumber() != null ? topCard.getNumber() : topCard.getAction()) + ")");

            // 5. Send the full game state to all clients
            var gameState = session.getGameState();
            gameState.put("playerHands", session.getPlayerHands());
            sessionManager.broadcastGameState(gameId);

            // 6. Also send the start event with first player
            var startMsg = new HashMap<String, Object>();
            startMsg.put("firstPlayer", players.get(0));
            startMsg.put("gameState", gameState);
            sessionManager.broadcastToStart(gameId, startMsg);

            System.out.println("[WebSocketController] Sent start event and game state for game #" + gameId);
        } catch (Exception e) {
            System.err.println("[WebSocketController] Failed to initialize game #" + gameId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @MessageMapping("/game/{gameId}/play")
    public void playCard(@DestinationVariable Long gameId, @Payload PlayerCardRequest request, Principal principal) {
        System.out.println("[WebSocket][REQUEST] /game/" + gameId + "/play from user: " + principal.getName() + ", payload: " + request);
        GameSession session = sessionManager.getSession(gameId);

        if (session != null && session.isPlayerTurn(principal.getName())) {
            Game game = gameRepository.findById(gameId).orElseThrow(() ->
                new IllegalArgumentException("Game not found"));

            Card card = cardRepository.findById(request.getCardId()).orElseThrow(() ->
                new IllegalArgumentException("Card not found"));

            // Validate and process the move based on UNO rules
            boolean isValidMove = validateMove(game, card);

            if (isValidMove) {
                // Update game state
                game.setTopCard(card);
                gameRepository.save(game);

                // Handle special cards
                handleSpecialCardEffects(session, card);

                // Move to next player
                session.nextTurn();

                // Broadcast updated game state
                sessionManager.broadcastGameState(gameId);
            } else {
                // Send error message to player
                sessionManager.sendMessageToPlayer(gameId, principal.getName(), new ErrorMessage("Invalid move"));
            }
        }
    }

    @MessageMapping("/game/{gameId}/draw")
    public void drawCard(@DestinationVariable Long gameId, Principal principal) {
        System.out.println("[WebSocket][REQUEST] /game/" + gameId + "/draw from user: " + principal.getName());
        // Implementation for drawing a card
    }

    @MessageMapping("/game/{gameId}/challenge")
    public void challengeWildDrawFour(@DestinationVariable Long gameId, Principal principal) {
        System.out.println("[WebSocket][REQUEST] /game/" + gameId + "/challenge from user: " + principal.getName());
        // Implementation for challenging Wild Draw Four
    }

    private boolean validateMove(Game game, Card card) {
        // Logic to validate if the move follows UNO rules
        return true; // Placeholder
    }

    private void handleSpecialCardEffects(GameSession session, Card card) {
        // Logic for handling effects of special cards
        if (card.getAction() == Card.CardAction.SKIP) {
            session.skipTurn();
        } else if (card.getAction() == Card.CardAction.REVERSE) {
            session.reverseDirection();
        }
        // Handle other special cards
    }
}
