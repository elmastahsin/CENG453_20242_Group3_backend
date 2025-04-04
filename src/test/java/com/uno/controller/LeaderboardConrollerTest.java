package com.uno.controller;

import com.uno.dtos.LeaderboardEntryDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    @Mock
    private LeaderboardService leaderboardService;

    @InjectMocks
    private LeaderboardController leaderboardController;

    private List<LeaderboardEntryDTO> mockLeaderboardEntries;
    private ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> mockResponse;

    @BeforeEach
    void setUp() {
        // Create mock data
        mockLeaderboardEntries = new ArrayList<>();
        mockLeaderboardEntries.add(new LeaderboardEntryDTO(1L, 100001L, "user1", 500L, 
                LocalDateTime.now(), 1L, "SINGLE_PLAYER"));
        mockLeaderboardEntries.add(new LeaderboardEntryDTO(2L, 100002L, "user2", 450L, 
                LocalDateTime.now(), 2L, "TWO_PLAYER"));
        
        mockResponse = ResponseEntity.ok(new GeneralResponseWithData<>(
                new Status(HttpStatus.OK, "Success"),
                mockLeaderboardEntries
        ));
    }

    @Test
    void getWeeklyLeaderboardShouldReturnData() {
        // Arrange
        when(leaderboardService.getWeeklyLeaderboard(anyString())).thenReturn(mockResponse);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardController.getWeeklyLeaderboard("");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockLeaderboardEntries.size(), response.getBody().getData().size());
    }

    @Test
    void getWeeklyLeaderboardWithGameTypeFilterShouldReturnData() {
        // Arrange
        when(leaderboardService.getWeeklyLeaderboard("TWO_PLAYER")).thenReturn(mockResponse);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardController.getWeeklyLeaderboard("TWO_PLAYER");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockLeaderboardEntries.size(), response.getBody().getData().size());
    }

    @Test
    void getMonthlyLeaderboardShouldReturnData() {
        // Arrange
        when(leaderboardService.getMonthlyLeaderboard(anyString())).thenReturn(mockResponse);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardController.getMonthlyLeaderboard("");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockLeaderboardEntries.size(), response.getBody().getData().size());
    }

    @Test
    void getMonthlyLeaderboardWithGameTypeFilterShouldReturnData() {
        // Arrange
        when(leaderboardService.getMonthlyLeaderboard("SINGLE_PLAYER")).thenReturn(mockResponse);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardController.getMonthlyLeaderboard("SINGLE_PLAYER");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockLeaderboardEntries.size(), response.getBody().getData().size());
    }

    @Test
    void getAllTimeLeaderboardShouldReturnData() {
        // Arrange
        when(leaderboardService.getAllTimeLeaderboard(anyString())).thenReturn(mockResponse);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardController.getAllTimeLeaderboard("");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockLeaderboardEntries.size(), response.getBody().getData().size());
    }

    @Test
    void getAllTimeLeaderboardWithGameTypeFilterShouldReturnData() {
        // Arrange
        when(leaderboardService.getAllTimeLeaderboard("FOUR_PLAYER")).thenReturn(mockResponse);

        // Act
        ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> response = 
                leaderboardController.getAllTimeLeaderboard("FOUR_PLAYER");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockLeaderboardEntries.size(), response.getBody().getData().size());
    }
}