package com.uno.controller;

import com.uno.dtos.GameRequestDTO;
import com.uno.dtos.JoinGameRequestDTO;
import com.uno.service.GameService;
import com.uno.websocket.GameSessionManager;
import com.uno.websocket.GameSession;
import com.uno.repository.GamePlayerRepository;
import com.uno.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/game")
@Tag(name = "Game Controller", description = "Game Controller endpoints")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class); // Added logger
    private final GameService gameService;
    private final GameSessionManager gameSessionManager;
    private final GamePlayerRepository gamePlayerRepository; // Added GamePlayerRepository
    private final UserRepository userRepository; // Added UserRepository

    @Autowired
    public GameController(GameService gameService, GameSessionManager gameSessionManager, GamePlayerRepository gamePlayerRepository, UserRepository userRepository) {
        this.gameService = gameService;
        this.gameSessionManager = gameSessionManager;
        this.gamePlayerRepository = gamePlayerRepository; // Initialize GamePlayerRepository
        this.userRepository = userRepository; // Initialize UserRepository
    }

    @PostMapping("/start")
    public ResponseEntity<?> startGame(@Valid @RequestBody GameRequestDTO gameRequestDTO) {
        return gameService.startGame(gameRequestDTO);

    }

    @GetMapping("/status/{id}")
    public ResponseEntity<?> getGameStatus(@PathVariable ("id") Long id) {
        return gameService.getGameStatus(id);
    }

    @PostMapping("/status/{id}/update/{status}")
    public ResponseEntity<?> updateGameStatus(@PathVariable ("id") Long id,
                                              @PathVariable ("status") String status) {
        return gameService.updateGameStatus(id,status);
    }

    @PostMapping("/end")
    public ResponseEntity<?> endGame(@Valid @RequestBody GameRequestDTO gameRequestDTO) {
        return gameService.endGame(gameRequestDTO);
    }

    @GetMapping("/top-card/{id}")
    public ResponseEntity<?> getTopCard(@PathVariable ("id") Long id) {
        return gameService.getTopCard(id);
    }
    @PostMapping("/top-card/{gameId}/update/{topCardId}")
    public ResponseEntity<?> updateTopCard(@PathVariable ("gameId") Long gameId,
                                           @PathVariable ("topCardId") Integer topCardId) {
        return gameService.updateTopCard(gameId, topCardId);
    }

    @GetMapping("/lobby")
    public ResponseEntity<?> getGameHistory() {
        try {
            return gameService.getLobbyGames();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving game lobby: " + e.getMessage());
        }
    }


    @PostMapping("/create")
    public ResponseEntity<?> createGame(@Valid @RequestBody GameRequestDTO gameRequestDTO) {
        ResponseEntity<?> response = gameService.startGame(gameRequestDTO);
        // After creating game in DB, create a WebSocket session for it
        try {
            // Get the game ID from response
            var responseBody = response.getBody();
            if (responseBody instanceof java.util.Map) {
                var map = (java.util.Map<?, ?>) responseBody;
                if (map.containsKey("data")) {
                    var data = map.get("data");
                    if (data instanceof java.util.Map) {
                        var dataMap = (java.util.Map<?, ?>) data;
                        if (dataMap.containsKey("gameId")) {
                            Long gameId = Long.valueOf(dataMap.get("gameId").toString());
                            System.out.println("[GameController] Creating WebSocket session for new game #" + gameId);
                            // Create the WebSocket session for this game
                            gameSessionManager.createSession(gameId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to create WebSocket session: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGame(@Valid @RequestBody JoinGameRequestDTO gameRequestDTO) {
        ResponseEntity<?> response = gameService.joinGame(gameRequestDTO);
        // Ensure player is added to in-memory session and broadcast lobby/start events
        Long gameId = gameRequestDTO.getGameId();
        String username = gameRequestDTO.getUsername();

        // If no session exists yet, create one first
        GameSession session = gameSessionManager.getSession(gameId);
        if (session == null) {
            System.out.println("[GameController] Creating missing WebSocket session for game #" + gameId);
            gameSessionManager.createSession(gameId);
            session = gameSessionManager.getSession(gameId);
        }

        if (session != null) {
            // Add player to session if not already present
            var gamePlayer = gameSessionManager.getGamePlayerRepository().findByGameIdAndUsername(gameId, username);
            if (gamePlayer != null) {
                System.out.println("[GameController] Adding player " + username + " to game #" + gameId);
                session.addPlayer(gamePlayer);

                // Broadcast lobby state
                var players = session.getPlayers().stream().map(p -> p.getUser().getUsername()).toList();
                boolean ready = players.size() >= 2; // Change 2 to required player count if needed
                var lobbyState = new java.util.HashMap<String, Object>();
                lobbyState.put("gameId", gameId);
                lobbyState.put("players", players);
                lobbyState.put("ready", ready);

                System.out.println("[GameController] Broadcasting lobby state for game #" + gameId + ": " + players.size() + " players, ready=" + ready);
                gameSessionManager.broadcastToLobby(gameId, lobbyState);

                if (ready) {
                    System.out.println("[GameController] Broadcasting game start for game #" + gameId + ", first player: " + players.get(0));
                    var startMsg = new java.util.HashMap<String, Object>();
                    startMsg.put("firstPlayer", players.get(0));
                    gameSessionManager.broadcastToStart(gameId, startMsg);
                }
            } else {
                System.out.println("[GameController] WARNING: Could not find GamePlayer for " + username + " in game #" + gameId);
            }
        } else {
            System.out.println("[GameController] ERROR: Failed to get or create session for game #" + gameId);
        }
        return response;
    }
}

