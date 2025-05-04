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
    private String winnerUsername;

    public GameRequestDTO(Long id, GameStatus status, LocalDateTime startDate, LocalDateTime endDate, GameType gameType, Integer topCardId, Boolean multiplayer, String winnerUsername) {
        this.id = id;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gameType = gameType;
        this.topCardId = topCardId;
        this.multiplayer = multiplayer;
        this.winnerUsername = winnerUsername;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public Integer getTopCardId() {
        return topCardId;
    }

    public void setTopCardId(Integer topCardId) {
        this.topCardId = topCardId;
    }

    public Boolean getMultiplayer() {
        return multiplayer;
    }

    public void setMultiplayer(Boolean multiplayer) {
        this.multiplayer = multiplayer;
    }

    public String getWinnerUsername() {
        return winnerUsername;
    }

    public void setWinnerUsername(String winnerUsername) {
        this.winnerUsername = winnerUsername;
    }
}
