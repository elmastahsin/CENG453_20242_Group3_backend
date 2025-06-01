package com.uno.repository;

import com.uno.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByStatusOrderByEndDateDesc(Game.GameStatus gameStatus);

    Object countByStatus(Game.GameStatus gameStatus);
}
