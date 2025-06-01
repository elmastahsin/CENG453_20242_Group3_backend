package com.uno.controller;

import com.uno.dtos.GameStateMessage;
import com.uno.dtos.PlayCardMessage;
import com.uno.service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {


    private final GameService gameService; // Your existing game logic service

    public GameWebSocketController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/play/{gameId}")
    @SendTo("/topic/game/{gameId}")
    public GameStateMessage playCard(@DestinationVariable String gameId, PlayCardMessage message) throws Exception {
        synchronized (this) {
            // Validate and process the move atomically
            GameStateMessage updatedState = gameService.processMove(gameId, message);
            return updatedState;  // Broadcast updated game state to all subscribed clients
        }
    }
}
