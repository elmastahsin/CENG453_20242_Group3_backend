package com.uno.controller;

import com.uno.dtos.GameRequestDTO;
import com.uno.dtos.JoinGameRequestDTO;
import com.uno.service.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return gameService.startGame(gameRequestDTO);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGame(@Valid @RequestBody JoinGameRequestDTO gameRequestDTO) {
        // Logic to join a game can be implemented here
        return gameService.joinGame(gameRequestDTO);

    }
}
