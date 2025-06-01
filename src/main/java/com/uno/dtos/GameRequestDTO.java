package com.uno.dtos;

import com.uno.entity.Game.GameStatus;
import com.uno.entity.Game.GameType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRequestDTO {
    private Long id;
    private GameStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private GameType gameType;
    private Integer topCardId;
    private Boolean multiplayer;
    private String winnerUsername;
}