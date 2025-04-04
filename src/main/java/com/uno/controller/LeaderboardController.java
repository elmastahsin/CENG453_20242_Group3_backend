package com.uno.controller;

import com.uno.dtos.LeaderboardEntryDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@Tag(name = "Leaderboard", description = "Leaderboard endpoints")
public class LeaderboardController {
    
    private final LeaderboardService leaderboardService;
    
    @Autowired
    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }
    
    @Operation(summary = "Get Weekly Leaderboard", description = "Retrieve the leaderboard for the past week")
    @GetMapping("/weekly")
    public ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> getWeeklyLeaderboard(
            @Parameter(description = "Filter by game type (optional)")
            @RequestParam(required = false) String gameType) {
        return leaderboardService.getWeeklyLeaderboard(gameType);
    }
    
    @Operation(summary = "Get Monthly Leaderboard", description = "Retrieve the leaderboard for the past month")
    @GetMapping("/monthly")
    public ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> getMonthlyLeaderboard(
            @Parameter(description = "Filter by game type (optional)")
            @RequestParam(required = false) String gameType) {
        return leaderboardService.getMonthlyLeaderboard(gameType);
    }
    
    @Operation(summary = "Get All-Time Leaderboard", description = "Retrieve the all-time leaderboard")
    @GetMapping("/all-time")
    public ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> getAllTimeLeaderboard(
            @Parameter(description = "Filter by game type (optional)")
            @RequestParam(required = false) String gameType) {
        return leaderboardService.getAllTimeLeaderboard(gameType);
    }
}