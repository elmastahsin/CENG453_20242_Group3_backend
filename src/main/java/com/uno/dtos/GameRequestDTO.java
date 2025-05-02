package com.uno.dtos;

import com.uno.entity.Game.GameStatus;
import com.uno.entity.Game.GameType;
import java.time.LocalDateTime;

public class GameRequestDTO {
    private Long id;
    private GameStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private GameType gameType;
    private Integer topCardId;
    private Boolean multiplayer;
    private Long winnerId;

    public GameRequestDTO(Long id,
                   GameStatus status,
                   LocalDateTime startDate,
                   LocalDateTime endDate,
                   GameType gameType,
                   Integer topCardId,
                   Boolean multiplayer,
                   Long winnerId) {
        this.id = id;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gameType = gameType;
        this.topCardId = topCardId;
        this.multiplayer = multiplayer;
        this.winnerId = winnerId;
    }

    public Long getId() {
        return id;
    }

    public GameStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public GameType getGameType() {
        return gameType;
    }

    public Integer getTopCardId() {
        return topCardId;
    }

    public Boolean isMultiplayer() {
        return multiplayer;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    @Override
    public String toString() {
        return "GameDTO{" +
                "id=" + id +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", gameType=" + gameType +
                ", topCardId=" + topCardId +
                ", multiplayer=" + multiplayer +
                ", winnerId=" + winnerId +
                '}';
    }
}
