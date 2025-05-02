package com.uno.controller;

import com.uno.service.CardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card")
@Tag(name = "Card Controller", description = "Card Controller endpoints")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }


    //card list
    @GetMapping("/list")
    public ResponseEntity<?> getCardList() {
        return cardService.getCardList();
    }

}
