package com.uno.dtos;

import com.uno.entity.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data

@NoArgsConstructor
public class LobbyResponse {
    private List<GameResponseDTO> games;

    public LobbyResponse(List<GameResponseDTO> games) {
        this.games = games;
    }
}
