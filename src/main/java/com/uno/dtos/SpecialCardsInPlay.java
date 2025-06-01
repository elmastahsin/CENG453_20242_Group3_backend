package com.uno.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class SpecialCardsInPlay {
    private boolean hasDrawPenalty;      // Whether there's a draw penalty active
    private boolean requiresColorChoice; // Whether a color choice is required

    // Additional flags you might need
    private boolean skipActive;          // Whether next player is skipped
    private String forcedColor;          // Color that must be played (from Wild cards)

    public SpecialCardsInPlay(boolean hasDrawPenalty, boolean requiresColorChoice) {
        this.hasDrawPenalty = hasDrawPenalty;
        this.requiresColorChoice = requiresColorChoice;
        this.skipActive = false;
        this.forcedColor = null;
    }
}