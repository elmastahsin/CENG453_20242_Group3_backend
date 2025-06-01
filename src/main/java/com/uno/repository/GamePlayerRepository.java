package com.uno.repository;

import com.uno.entity.Game;
import com.uno.entity.GamePlayer;
import com.uno.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    @Query("SELECT gp FROM GamePlayer gp WHERE gp.game.gameId = :id ORDER BY gp.id ASC")
    List<GamePlayer> findByGameIdOrderByIdAsc(@Param("id") Long id);

    @Query("SELECT COUNT(gp) FROM GamePlayer gp WHERE gp.game.gameId = :id")
    int countByGameId(@Param("id") Long id);

    @Query("SELECT gp FROM GamePlayer gp WHERE gp.game.gameId = :gameId AND gp.user.username = :username")
    GamePlayer findByGameIdAndUsername(@Param("gameId") Long gameId, @Param("username") String username);

    // Added method to check if a user is already in a game
    boolean existsByGameAndUser(Game game, User user);
}
