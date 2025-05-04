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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        List<LeaderboardEntryDTO> result = aggregateScoresByUser(leaderboardEntries);

        return ResponseEntity.ok(new GeneralResponseWithData<>(
                new Status(HttpStatus.OK, "All-time leaderboard retrieved successfully"),
                result
        ));
    }

    private ResponseEntity<GeneralResponseWithData<List<LeaderboardEntryDTO>>> getLeaderboardSince(
            LocalDateTime since, String gameType, String timePeriod) {

        List<String> validGameTypes = List.of("SINGLE_PLAYER","TWO_PLAYER","THREE_PLAYER","FOUR_PLAYER");
        List<Leaderboard> leaderboardEntries;

        if (gameType != null && !gameType.isEmpty()) {
            if (!validGameTypes.contains(gameType.toUpperCase())) {
                return ResponseEntity.ok(new GeneralResponseWithData<>(
                        new Status(HttpStatus.BAD_REQUEST, "Invalid game type"),
                        new ArrayList<>()
                ));
            }
            leaderboardEntries = leaderboardRepository.findTopScoresByGameTypeSince(gameType, since);
        } else {
            leaderboardEntries = leaderboardRepository.findTopScoresSince(since);
        }

        List<LeaderboardEntryDTO> result = aggregateScoresByUser(leaderboardEntries);

        return ResponseEntity.ok(new GeneralResponseWithData<>(
                new Status(HttpStatus.OK, timePeriod + " leaderboard retrieved successfully"),
                result
        ));
    }

    private List<LeaderboardEntryDTO> aggregateScoresByUser(List<Leaderboard> leaderboardEntries) {
        // Use a map to aggregate scores by user
        Map<Long, UserScoreInfo> userScoreMap = new HashMap<>();

        // Process each leaderboard entry
        for (Leaderboard entry : leaderboardEntries) {
            Long userId = entry.getUser().getId();
            String username = entry.getUser().getUsername();
            Long score = entry.getScore();
            LocalDateTime scoreDate = entry.getScoreDate();

            // If user already exists in map, update the aggregated info
            if (userScoreMap.containsKey(userId)) {
                UserScoreInfo existingInfo = userScoreMap.get(userId);
                existingInfo.totalScore += score;

                // Update the latest date if current entry is more recent
                if (scoreDate.isAfter(existingInfo.latestDate)) {
                    existingInfo.latestDate = scoreDate;
                }
            } else {
                // Create a new entry for this user
                UserScoreInfo newInfo = new UserScoreInfo();
                newInfo.userId = userId;
                newInfo.username = username;
                newInfo.totalScore = score;
                newInfo.latestDate = scoreDate;
                userScoreMap.put(userId, newInfo);
            }
        }

        // Convert the map entries to DTOs and sort them
        List<LeaderboardEntryDTO> result = new ArrayList<>();
        for (UserScoreInfo info : userScoreMap.values()) {
            LeaderboardEntryDTO dto = new LeaderboardEntryDTO(
                    null, // No specific ID for aggregated entry
                    info.userId,
                    info.username,
                    info.totalScore,
                    info.latestDate,
                    null, // No specific game ID for aggregated entry
                    null  // Game type not tracked in aggregation
            );
            result.add(dto);
        }

        // Sort the results by score in descending order
        result.sort((a, b) -> Long.compare(b.getScore(), a.getScore()));

        return result;
    }

    // Helper class to store aggregated user score information
    private static class UserScoreInfo {
        Long userId;
        String username;
        Long totalScore = 0L;
        LocalDateTime latestDate;
        // Game type could be added here if needed for filtering
    }
}