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
            // Add player to session
            String username = principal.getName();
            var gamePlayer = gamePlayerRepository.findByGameIdAndUsername(gameId, username);
            if (gamePlayer != null) {
                session.addPlayer(gamePlayer);
            }
            // Broadcast lobby state to /topic/game/{gameId}/lobby
            var players = session.getPlayers().stream()
                .filter(p -> p != null && p.getUser() != null) // Added null checks
                .map(p -> p.getUser().getUsername())
                .toList();
            boolean ready = players.size() >= 2; // Change 2 to required player count if needed
            var lobbyState = new java.util.HashMap<String, Object>();
            lobbyState.put("gameId", gameId);
            lobbyState.put("players", players);
            lobbyState.put("ready", ready);
            sessionManager.broadcastToLobby(gameId, lobbyState);
            // If ready, broadcast start event
            if (ready) {
                System.out.println("[WebSocketController] Game #" + gameId + " is ready with " + players.size() + " players - initializing game!");

                // Initialize the game (deal cards, set top card, etc)
                try {
                    // Get the game from database
                    Game game = gameRepository.findById(gameId).orElseThrow();

                    // Initialize a deck and deal cards
                    session.initializeGameDeck(cardRepository);

                    // Deal cards to players
                    for (var player : session.getPlayers()) {
                        session.dealInitialCardsToPlayer(player, cardRepository);
                    }

                    // Set the initial top card
                    Card topCard = session.drawCardFromDeck(cardRepository);
                    game.setTopCard(topCard);
                    gameRepository.save(game);

                    System.out.println("[WebSocketController] Game #" + gameId + " initialized with top card: " + topCard.getId() + " (" + topCard.getColor() + " " + topCard.getNumber() + ")");

                    // Update game status in database to STARTED
                    game.setStatus(Game.GameStatus.STARTED);
                    gameRepository.save(game);

                    // Send the full game state to all clients
                    var gameState = session.getGameState();
                    gameState.put("playerHands", session.getPlayerHands()); // Include cards in each player's hand
                    sessionManager.broadcastGameState(gameId);

                    // Also send the start event
                    var startMsg = new java.util.HashMap<String, Object>();
                    startMsg.put("firstPlayer", players.get(0));
                    startMsg.put("gameState", gameState);
                    sessionManager.broadcastToStart(gameId, startMsg);

                    System.out.println("[WebSocketController] Sent start event and game state for game #" + gameId);
                } catch (Exception e) {
                    System.err.println("[WebSocketController] Failed to initialize game #" + gameId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    @MessageMapping("/game/{gameId}/play")
    public void playCard(@DestinationVariable Long gameId, @Payload PlayerCardRequest request, Principal principal) {
        System.out.println("[WebSocket][REQUEST] /game/" + gameId + "/play from user: " + principal.getName() + ", payload: " + request);
        GameSession session = sessionManager.getSession(gameId);

        if (session != null && session.isPlayerTurn(principal.getName())) {
            Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));

            Card card = cardRepository.findById(request.getCardId()).orElseThrow(() -> new IllegalArgumentException("Card not found"));

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

