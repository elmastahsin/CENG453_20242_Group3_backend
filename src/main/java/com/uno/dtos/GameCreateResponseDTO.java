package com.uno.dtos;

import com.uno.entity.Game;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameCreateResponseDTO {
    private Long gameId;
    private Game.GameType gameType;

}
