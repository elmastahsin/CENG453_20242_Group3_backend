package com.uno.dtos;
import com.uno.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayCardMessage {
    private String playerId;       // ID of the player making the move
    private String gameId;         // ID of the game session
    private Card cardPlayed;       // The card the player wants to play
    private boolean declaredUNO;   // Whether the player declared UNO with this move
    private boolean challenged;    // Whether the player is challenging a Wild Draw Four (optional)

    // Optional fields for additional actions
    // private boolean drawCard;   // If the player chooses to draw instead of play a card
}
