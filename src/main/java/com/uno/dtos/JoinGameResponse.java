package com.uno.dtos;

import com.uno.dtos.responseDto.PlayerDTO;
import com.uno.entity.Game;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


@Data
@AllArgsConstructor
public class JoinGameResponse {
    private boolean success;
    private Game.GameType gameType;
    private List<PlayerDTO> players;
}

