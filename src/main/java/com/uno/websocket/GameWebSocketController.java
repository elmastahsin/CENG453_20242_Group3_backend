package com.uno.websocket;

import com.uno.entity.Card;
import com.uno.entity.Game;
import com.uno.repository.CardRepository;
import com.uno.repository.GameRepository;
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

    public GameWebSocketController(GameSessionManager sessionManager, CardRepository cardRepository, GameRepository gameRepository) {
        this.sessionManager = sessionManager;
        this.cardRepository = cardRepository;
        this.gameRepository = gameRepository;
    }

    @MessageMapping("/game/{gameId}/join")
    public void joinGame(@DestinationVariable Long gameId, Principal principal) {
        GameSession session = sessionManager.getSession(gameId);
        if (session != null) {
            // Add player to session
            sessionManager.broadcastGameState(gameId);
        }
    }

    @MessageMapping("/game/{gameId}/play")
    public void playCard(@DestinationVariable Long gameId, @Payload PlayerCardRequest request, Principal principal) {
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
        // Implementation for drawing a card
    }

    @MessageMapping("/game/{gameId}/challenge")
    public void challengeWildDrawFour(@DestinationVariable Long gameId, Principal principal) {
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