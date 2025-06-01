package com.uno.service;
import com.uno.dtos.GameRequestDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.entity.Game;
import com.uno.repository.CardRepository;
import com.uno.repository.GameRepository;
import com.uno.repository.LeaderboardRepository;
import com.uno.repository.UserRepository;
import com.uno.service.impl.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);  // Initializes mocks
    }

    @Test
    public void testStartGame() {
        GameRequestDTO gameRequestDTO = new GameRequestDTO();
        gameRequestDTO.setGameType(Game.GameType.SINGLE_PLAYER);
        gameRequestDTO.setStatus(Game.GameStatus.PENDING);

        // Mocking repository save
        Game game = new Game();
        game.setId(1L);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Call the method to test
        ResponseEntity<?> response = gameService.startGame(gameRequestDTO);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        GeneralResponseWithData<Status> responseBody = (GeneralResponseWithData<Status>) response.getBody();
        assertEquals("Game started successfully", responseBody.getData().getDescription());
        assertEquals(1L, responseBody.getData().getDescription());
    }
}
