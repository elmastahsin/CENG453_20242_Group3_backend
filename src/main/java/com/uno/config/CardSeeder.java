package com.uno.config;

import com.uno.entity.Card;
import com.uno.entity.Card.CardAction;
import com.uno.entity.Card.CardColor;
import com.uno.entity.Card.CardType;
import com.uno.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CardSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CardSeeder.class);
    private final CardRepository cardRepository;
    private int currentId = 1; // For assigning IDs

    @Override
    public void run(ApplicationArguments args) {
        if (cardRepository.count() > 0) {
            log.info("Card table already seeded.");
            return; // already seeded
        }

        List<Card> cards = new ArrayList<>();

        for (CardColor color : List.of(CardColor.RED, CardColor.YELLOW, CardColor.GREEN, CardColor.BLUE)) {
            // 0 (one of each color)
            cards.add(createNumberCard(color, 0));
            // 1-9 (two of each color)
            for (int n = 1; n <= 9; n++) {
                cards.add(createNumberCard(color, n));
                cards.add(createNumberCard(color, n));
            }
            // Skip, Reverse, Draw-Two (two of each color)
            for (CardAction a : List.of(CardAction.SKIP, CardAction.REVERSE, CardAction.DRAW_TWO)) {
                cards.add(createActionCard(color, a));
                cards.add(createActionCard(color, a));
            }
        }
        // Four Wild & four Wild-Draw-Four
        for (int i = 0; i < 4; i++) {
            cards.add(createWildCard(CardAction.WILD));
            cards.add(createWildCard(CardAction.WILD_DRAW_FOUR));
        }
        cardRepository.saveAll(cards);
        log.info("Seeded {} UNO cards", cards.size()); // Should be 108
    }

    private Card createNumberCard(CardColor color, int number) {
        Card card = new Card();
        card.setId(currentId++);
        card.setColor(color);
        card.setNumber(number);
        card.setCardType(CardType.STANDARD);
        card.setAction(null); // Number cards don't have actions
        return card;
    }

    private Card createActionCard(CardColor color, CardAction action) {
        Card card = new Card();
        card.setId(currentId++);
        card.setColor(color);
        card.setAction(action);
        card.setCardType(CardType.STANDARD); // Colored action cards are STANDARD type
        card.setNumber(null); // Action cards don't have numbers
        return card;
    }

    private Card createWildCard(CardAction action) {
        Card card = new Card();
        card.setId(currentId++);
        card.setColor(CardColor.MULTI); // Wild cards have MULTI color
        card.setAction(action);
        card.setCardType(CardType.WILDCARD); // Wild cards are WILDCARD type
        card.setNumber(null); // Wild cards don't have numbers
        return card;
    }
}

