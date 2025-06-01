package com.uno.repository;

import com.uno.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    @Query(
            "SELECT g FROM Game g WHERE g.status = :gameStatus AND g.isMultiplayer = true"
    )
    List<Game> getPendingAndIsMultiplayer(Game.GameStatus gameStatus);
}
