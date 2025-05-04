package com.uno.service.impl;

import com.uno.dtos.GameRequestDTO;
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

import java.time.LocalDateTime;

@Service
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final LeaderboardRepository leaderboardRepository;

    public GameServiceImpl(GameRepository gameRepository, CardRepository cardRepository, UserRepository userRepository, LeaderboardRepository leaderboardRepository) {
        this.gameRepository = gameRepository;
        this.cardRepository = cardRepository;

        this.userRepository = userRepository;
        this.leaderboardRepository = leaderboardRepository;
    }

    public Game toEntity(GameRequestDTO gameDTO) {
        Game game = new Game();
        game.setStatus(gameDTO.getStatus());
        game.setStartDate(LocalDateTime.now());
        game.setEndDate(gameDTO.getEndDate());
        game.setGameType(gameDTO.getGameType());
        Card card = cardRepository.findById(gameDTO.getTopCardId()).orElseThrow(() -> new IllegalArgumentException("Card not found"));
        game.setTopCard(card);
        game.setIsMultiplayer(gameDTO.getMultiplayer());
        User user = userRepository.findByUsername(gameDTO.getWinnerUsername()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        game.setWinner(user);


        return game;


    }

    @Override
    public ResponseEntity<?> startGame(GameRequestDTO gameRequestDTO) {
        // Validate the game request DTO
        if (gameRequestDTO == null || gameRequestDTO.getGameType() == null || gameRequestDTO.getStatus() == Game.GameStatus.COMPLETED) {
            throw new IllegalArgumentException("Invalid game request");
        }

//         Create a new game entity and save it to the repository
        Game game = new Game();
        game.setStatus(Game.GameStatus.PENDING);
        game.setStartDate(LocalDateTime.now());
        game.setGameType(gameRequestDTO.getGameType());
        game.setIsMultiplayer(gameRequestDTO.getMultiplayer());
        gameRepository.save(game);
        return ResponseEntity.ok(new GeneralResponseWithData<>(new Status(HttpStatus.OK, "Game started successfully"), game.getId()));


    }

    @Override
    public ResponseEntity<?> endGame(GameRequestDTO gameRequestDTO) {

        // Validate the game request DTO
        if (gameRequestDTO == null || gameRequestDTO.getId() == null) {
            throw new IllegalArgumentException("Invalid game request");
        }

        // Find the existing game entity by ID
        Game game = gameRepository.findById(gameRequestDTO.getId()).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        // Update the game status to COMPLETED
        game.setStatus(Game.GameStatus.COMPLETED);
        game.setEndDate(LocalDateTime.now());
        game.setTopCard(null); // Set the top card to null or any other logic you want

        game.setWinner(userRepository.findByUsername(gameRequestDTO.getWinnerUsername()).orElseThrow(() -> new IllegalArgumentException("User not found")));

        // Save the updated game entity to the repository
        gameRepository.save(game);
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setGame(game);
        leaderboard.setUser(game.getWinner());
        leaderboard.setScore(1L);
        leaderboard.setScoreDate(LocalDateTime.now());
        leaderboardRepository.save(leaderboard);

        return ResponseEntity.ok(new GeneralResponseWithData<>(new Status(HttpStatus.OK, "Game ended successfully"),  leaderboard.getUser().getUsername()));

    }

    @Override
    public ResponseEntity<?> getGameStatus(Long id) {
        // Retrieve the game status from the repository
        Game game = gameRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        // Return the game status as a response entity
        return ResponseEntity.ok(new GeneralResponseWithData<>(new Status(HttpStatus.OK, "Game status retrieved successfully"), game.getStatus()));
    }

    @Override
    public ResponseEntity<?> getGameHistory() {
        return null;
    }

    @Override
    public ResponseEntity<?> getGameStatistics() {
        return null;
    }

    @Override
    public ResponseEntity<?> getTopCard(Long id) {
        Game game = gameRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return ResponseEntity.ok(new GeneralResponseWithData<>(new Status(HttpStatus.OK, "Top card retrieved successfully"), game.getTopCard()));
    }

    @Override
    public ResponseEntity<?> updateTopCard(Long gameId, Integer topCardId) {
        // Retrieve the game entity from the repository
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        // Retrieve the top card entity from the repository
        Card topCard = cardRepository.findById(topCardId).orElseThrow(() -> new IllegalArgumentException("Top card not found"));
        // Update the top card of the game
        game.setTopCard(topCard);
        // Save the updated game entity to the repository
        gameRepository.save(game);
        return ResponseEntity.ok(new GeneralResponseWithData<>(new Status(HttpStatus.OK, "Top card updated successfully"), game.getTopCard()));
    }

    @Override
    public ResponseEntity<?> updateGameStatus(Long id, String status) {
        // Retrieve the game status from the repository
        Game game = gameRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Game not found"));
        // Update the game status to COMPLETED
        if (status.equalsIgnoreCase("COMPLETED")) {
            game.setStatus(Game.GameStatus.COMPLETED);
        } else if (status.equalsIgnoreCase("PLAYING")) {
            game.setStatus(Game.GameStatus.PLAYING);
        } else if (status.equalsIgnoreCase("PENDING")) {
            game.setStatus(Game.GameStatus.PENDING);
        } else {
            return ResponseEntity.badRequest().body(new GeneralResponseWithData<>(new Status(HttpStatus.BAD_REQUEST, "Invalid status"), null));
        }

        // Save the updated game entity to the repository
        gameRepository.save(game);
        return ResponseEntity.ok(new GeneralResponseWithData<>(new Status(HttpStatus.OK, "Game status updated successfully"), game.getStatus()));
    }


}
