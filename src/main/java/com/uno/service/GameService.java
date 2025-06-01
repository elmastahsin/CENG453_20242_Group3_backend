package com.uno.service;


import com.uno.dtos.GameRequestDTO;
import com.uno.dtos.JoinGameRequestDTO;
import org.springframework.http.ResponseEntity;

public interface GameService {
    ResponseEntity<?> startGame(GameRequestDTO gameRequestDTO);
    ResponseEntity<?>  endGame(GameRequestDTO gameRequestDTO);
    ResponseEntity<?> getGameStatus(Long id);
    ResponseEntity<?> getGameHistory();
    ResponseEntity<?> getGameStatistics();

    ResponseEntity<?> updateGameStatus(Long id, String status);

    ResponseEntity<?> getTopCard(Long gameId);

    ResponseEntity<?> updateTopCard(Long gameId, Integer topCardId);

    ResponseEntity<?> getLobbyGames();

    ResponseEntity<?> joinGame(JoinGameRequestDTO joinGameRequestDTO);
}
