package com.uno.dtos;

import com.uno.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerState {
    private String playerId;                  // Player’s unique ID
    private String username;                  // Player’s display name
    private List<Card> hand;                  // Cards currently held by player
    private int score;                       // Player’s total score
    private boolean isActive;                // Whether player is active or disconnected
    private boolean hasDeclaredUNO;          // Whether player declared UNO
}
