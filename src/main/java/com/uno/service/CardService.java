package com.uno.service;

import com.uno.dtos.responseDto.GeneralResponseWithData;
import com.uno.dtos.responseDto.Status;
import com.uno.entity.Card;
import com.uno.repository.CardRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {
    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public ResponseEntity<?> getCardList() {
        //    private final CardColor color;
        //    private final CardType type;
        //    private final int value; // Relevant for number cards (0-9)
        //şu parametrelerle
        List<Card> cards = cardRepository.findAll();
        return ResponseEntity.ok(new GeneralResponseWithData<>(new Status(HttpStatus.OK
        , "Card list retrieved successfully"), cards));
    }
}
