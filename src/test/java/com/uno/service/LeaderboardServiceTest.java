package com.uno.service;

import com.uno.dtos.LeaderboardEntryDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.entity.Game;
import com.uno.entity.Leaderboard;
import com.uno.entity.User;
import com.uno.repository.LeaderboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private List<Leaderboard> mockLeaderboardEntries;
    private User mockUser1;
    private User mockUser2;
    private Game mockGame1;
    private Game mockGame2;

    @BeforeEach
    void setUp() {
        // Create mock users
        mockUser1 = new User();
        mockUser1.setUserId(100001L);
        mockUser1.setUsername("user1");

        mockUser2 = new User();
        mockUser2.setUserId(100002L);
        mockUser2.setUsername("user2");

        // Create mock games
        mockGame1 = new Game();
        mockGame1.setGameId(1L);
        mockGame1.setGameType(Game.GameType.SINGLE_PLAYER);

        mockGame2 = new Game();
        mockGame2.setGameId(2L);
        mockGame2.setGameType(Game.GameType.TWO_PLAYER);

        // Create mock leaderboard entries
        mockLeaderboardEntries = new ArrayList<>();
        
        Leaderboard entry1 = new Leaderboard();
        entry1.setId(1L);
        entry1.setUser(mockUser1);
        entry1.setScore(500L);
        entry1.setScoreDate(LocalDateTime.now());
        entry1.setGame(mockGame1);
        
        Leaderboard entry2 = new Leaderboard();
        entry2.setId(2L);
        entry2.setUser(mockUser2);
        entry2.setScore(450L);
        entry2.setScoreDate(LocalDateTime.now());
        entry2.setGame(mockGame2);
        
        mockLeaderboardEntries.add(entry1);
        mockLeaderboardEntries.add(entry2);
    }

    @Test
    void getWeeklyLeaderboardShouldReturnData() {
        // Arrange
        when(leaderboardRepository.findTopScoresSince(any(LocalDateTime.class)))
                .thenReturn(mockLeaderboardEntries);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardService.getWeeklyLeaderboard(null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getData().size());
        
        // Verify data is correctly mapped
        LeaderboardEntryDTO firstEntry = response.getBody().getData().get(0);
        assertEquals(1L, firstEntry.getId());
        assertEquals(100001L, firstEntry.getUserId());
        assertEquals("user1", firstEntry.getUsername());
        assertEquals(500L, firstEntry.getScore());
        assertEquals("SINGLE_PLAYER", firstEntry.getGameType());
    }

    @Test
    void getWeeklyLeaderboardWithGameTypeFilterShouldReturnData() {
        // Arrange
        List<Leaderboard> filteredEntries = new ArrayList<>();
        filteredEntries.add(mockLeaderboardEntries.get(1)); // Only TWO_PLAYER entry
        
        when(leaderboardRepository.findTopScoresByGameTypeSince(
                eq("TWO_PLAYER"), any(LocalDateTime.class)))
                .thenReturn(filteredEntries);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardService.getWeeklyLeaderboard("TWO_PLAYER");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("TWO_PLAYER", response.getBody().getData().get(0).getGameType());
    }

    @Test
    void getMonthlyLeaderboardShouldReturnData() {
        // Arrange
        when(leaderboardRepository.findTopScoresSince(any(LocalDateTime.class)))
                .thenReturn(mockLeaderboardEntries);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardService.getMonthlyLeaderboard(null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getData().size());
    }

    @Test
    void getAllTimeLeaderboardShouldReturnData() {
        // Arrange
        when(leaderboardRepository.findAllTimeTopScores()).thenReturn(mockLeaderboardEntries);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardService.getAllTimeLeaderboard(null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getData().size());
    }

    @Test
    void getAllTimeLeaderboardWithGameTypeFilterShouldReturnData() {
        // Arrange
        List<Leaderboard> filteredEntries = new ArrayList<>();
        filteredEntries.add(mockLeaderboardEntries.get(0)); // Only SINGLE_PLAYER entry
        
        when(leaderboardRepository.findAllTimeTopScoresByGameType("SINGLE_PLAYER"))
                .thenReturn(filteredEntries);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardService.getAllTimeLeaderboard("SINGLE_PLAYER");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("SINGLE_PLAYER", response.getBody().getData().get(0).getGameType());
    }

    @Test
    void getLeaderboardShouldHandleInvalidGameType() {
        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardService.getWeeklyLeaderboard("INVALID_TYPE");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());  // API still returns 200 OK
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getData().size());  // Empty result for invalid game type
        assertEquals("ERROR", response.getBody().getStatus().getDescription().toString());
    }
}