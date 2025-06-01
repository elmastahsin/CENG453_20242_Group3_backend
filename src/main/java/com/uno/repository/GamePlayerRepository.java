package com.uno.repository;

import com.uno.entity.GamePlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
    @Query("SELECT gp FROM GamePlayer gp WHERE gp.game.id = :id ORDER BY gp.id ASC")
    List<GamePlayer> findByGameIdOrderByIdAsc(@Param("id") Long id);

    @Query("SELECT COUNT(gp) FROM GamePlayer gp WHERE gp.game.id = :id")
    int countByGameId(@Param("id") Long id);
}