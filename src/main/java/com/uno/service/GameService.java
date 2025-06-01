package com.uno.service;


import com.uno.dtos.GameRequestDTO;
import com.uno.dtos.GameState;
import com.uno.dtos.GameStateMessage;
import com.uno.dtos.PlayCardMessage;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface GameService {
    ResponseEntity<?> startGame(GameRequestDTO gameRequestDTO);
    ResponseEntity<?>  endGame(GameRequestDTO gameRequestDTO);
    ResponseEntity<?> getGameStatus(Long id);
    ResponseEntity<?> getGameHistory();
    ResponseEntity<?> getGameStatistics();

    ResponseEntity<?> updateGameStatus(Long id, String status);

    ResponseEntity<?> getTopCard(Long gameId);

    ResponseEntity<?> updateTopCard(Long gameId, Integer topCardId);

    GameStateMessage processMove(String gameId, PlayCardMessage move);

    GameStateMessage getCurrentState(String gameId);

    GameState initializeGameState(String gameId, List<String> playerIds);
}
