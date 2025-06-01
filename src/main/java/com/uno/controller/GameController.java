package com.uno.controller;

import com.uno.dtos.GameRequestDTO;
import com.uno.dtos.GameStateMessage;
import com.uno.dtos.InitializeGameRequest;
import com.uno.service.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/api/game")
@Tag(name = "Game Controller", description = "Game Controller endpoints")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startGame(@Valid @RequestBody GameRequestDTO gameRequestDTO) {
        return gameService.startGame(gameRequestDTO);

    }
    @PostMapping("/initialize")
    public ResponseEntity<?> initializeGame(@RequestBody InitializeGameRequest request) {
        try {
            gameService.initializeGameState(request.getGameId(), request.getPlayerIds());
            return ResponseEntity.ok("Game initialized successfully: " + request.getGameId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to initialize game: " + e.getMessage());
        }
    }


//    @GetMapping("/status/{id}")
//    public ResponseEntity<?> getGameStatus(@PathVariable ("id") Long id) {
//        return gameService.getGameStatus(id);
//    }

//    @PostMapping("/status/{id}/update/{status}")
//    public ResponseEntity<?> updateGameStatus(@PathVariable ("id") Long id,
//                                              @PathVariable ("status") String status) {
//        return gameService.updateGameStatus(id,status);
//    }
    @GetMapping("/{gameId}/state")
    public GameStateMessage getGameState(@PathVariable String gameId) {
        return gameService.getCurrentState(gameId);
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
}
