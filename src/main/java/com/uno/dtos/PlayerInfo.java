package com.uno.dtos;

import com.uno.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Supporting classes
@Data
@NoArgsConstructor
@AllArgsConstructor
class PlayerInfo {
    private String playerId;
    private String username;
    private List<Card> hand;
    private int score;
    private boolean hasDeclaredUNO;

    // Convenience method to get hand size (useful for UI)
    public int getHandSize() {
        return hand != null ? hand.size() : 0;
    }

    // Check if player can win on next turn
    public boolean canWinNext() {
        return getHandSize() == 1;
    }
}
