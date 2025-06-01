package com.uno.dtos;

import com.uno.entity.Game;
import com.uno.entity.GamePlayer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class GameResponseDTO {
    private Long id;
    private Game.GameType gameType;
    private Game.GameStatus status;
    private Long hostId;
    private String hostUsername;
    private int playerCount;
    private Boolean isMultiplayer;

    public GameResponseDTO(Long id, Game.GameType gameType, Game.GameStatus status,
                           Long hostId, String hostUsername, int playerCount, Boolean isMultiplayer) {
        this.id = id;
        this.gameType = gameType;
        this.status = status;
        this.hostId = hostId;
        this.hostUsername = hostUsername;
        this.playerCount = playerCount;
        this.isMultiplayer = isMultiplayer;
    }

    // getters and setters
}