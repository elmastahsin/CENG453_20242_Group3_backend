package com.uno.service;

import com.uno.dtos.LeaderboardEntryDTO;
import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.entity.Leaderboard;
import com.uno.repository.LeaderboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {
    
    private final LeaderboardRepository leaderboardRepository;
    
    @Autowired
    public LeaderboardService(LeaderboardRepository leaderboardRepository) {
        this.leaderboardRepository = leaderboardRepository;
    }
    
    public ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> getWeeklyLeaderboard(String gameType) {
        LocalDateTime weekAgo = LocalDateTime.now().minus(1, ChronoUnit.WEEKS);
        return getLeaderboardSince(weekAgo, gameType, "Weekly");
    }
    
    public ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> getMonthlyLeaderboard(String gameType) {
        LocalDateTime monthAgo = LocalDateTime.now().minus(1, ChronoUnit.MONTHS);
        return getLeaderboardSince(monthAgo, gameType, "Monthly");
    }
    
    public ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> getAllTimeLeaderboard(String gameType) {
        List<Leaderboard> leaderboardEntries;
        
        if (gameType != null && !gameType.isEmpty()) {
            leaderboardEntries = leaderboardRepository.findAllTimeTopScoresByGameType(gameType);
        } else {
            leaderboardEntries = leaderboardRepository.findAllTimeTopScores();
        }
        
        List<LeaderboardEntryDTO> result = mapToDTO(leaderboardEntries);
        
        return ResponseEntity.ok(new GeneralResponseWithData<>(
                new Status(HttpStatus.OK, "Success"),result
        ));
    }
    
    private ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> getLeaderboardSince(
            LocalDateTime since, String gameType, String timePeriod) {
        
        List<Leaderboard> leaderboardEntries;
        
        if (gameType != null && !gameType.isEmpty()) {
            leaderboardEntries = leaderboardRepository.findTopScoresByGameTypeSince(gameType, since);
        } else {
            leaderboardEntries = leaderboardRepository.findTopScoresSince(since);
        }
        
        List<LeaderboardEntryDTO> result = mapToDTO(leaderboardEntries);
        
        return ResponseEntity.ok(new GeneralResponseWithData<>(
                new Status(HttpStatus.OK, "Success"),
                result
        ));
    }
    
    private List<LeaderboardEntryDTO> mapToDTO(List<Leaderboard> leaderboardEntries) {
        return leaderboardEntries.stream()
                .map(entry -> new LeaderboardEntryDTO(
                        entry.getId(),
                        entry.getUser().getId(),
                        entry.getUser().getUsername(),
                        entry.getScore(),
                        entry.getScoreDate(),
                        entry.getGame().getId(),
                        entry.getGame().getGameType().toString()
                ))
                .collect(Collectors.toList());
    }
}