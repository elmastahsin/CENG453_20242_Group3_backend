package com.uno.repository;

import com.uno.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {
    
    @Query("SELECT l FROM Leaderboard l JOIN FETCH l.user JOIN FETCH l.game WHERE l.scoreDate >= :startDate ORDER BY l.score DESC")
    List<Leaderboard> findTopScoresSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT l FROM Leaderboard l JOIN FETCH l.user JOIN FETCH l.game ORDER BY l.score DESC")
    List<Leaderboard> findAllTimeTopScores();
    
    @Query("SELECT l FROM Leaderboard l WHERE l.game.gameType = :gameType AND l.scoreDate >= :startDate ORDER BY l.score DESC")
    List<Leaderboard> findTopScoresByGameTypeSince(
            @Param("gameType") String gameType, 
            @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT l FROM Leaderboard l WHERE l.game.gameType = :gameType ORDER BY l.score DESC")
    List<Leaderboard> findAllTimeTopScoresByGameType(@Param("gameType") String gameType);


}