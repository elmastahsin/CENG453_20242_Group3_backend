package com.uno.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "game")
@RequiredArgsConstructor
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameId;

    @Enumerated(EnumType.ORDINAL)
    private GameStatus status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type")
    private GameType gameType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "top_card_id")
    private Card topCard;

    @Column(name = "is_multiplayer")
    private Boolean isMultiplayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    public Game(Long gameId, GameStatus status, LocalDateTime startDate, LocalDateTime endDate, GameType gameType, Card topCard, Boolean isMultiplayer, User winner) {
        this.gameId = gameId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gameType = gameType;
        this.topCard = topCard;
        this.isMultiplayer = isMultiplayer;
        this.winner = winner;
    }

    public enum GameStatus {
        PENDING, STARTED, PLAYING, COMPLETED, CANCELED
    }

    public enum GameType {
        SINGLE_PLAYER, TWO_PLAYER, THREE_PLAYER, FOUR_PLAYER
    }


}

