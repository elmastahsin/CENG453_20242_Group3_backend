package com.uno.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "card")
public class Card {

    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    private CardColor color;

    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type")
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    private CardAction action;

    public enum CardColor {
        GREEN, RED, BLUE, YELLOW, MULTI
    }

    public enum CardType {
        WILDCARD, STANDARD
    }

    public enum CardAction {
        SKIP, REVERSE, DRAW_TWO, DRAW_FOUR, WILD
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CardColor getColor() {
        return color;
    }

    public void setColor(CardColor color) {
        this.color = color;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public CardAction getAction() {
        return action;
    }

    public void setAction(CardAction action) {
        this.action = action;
    }
}