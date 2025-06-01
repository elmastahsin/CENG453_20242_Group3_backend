package com.uno.websocket;

import com.uno.entity.Game;
import com.uno.entity.GamePlayer;
import com.uno.entity.Card;
import com.uno.repository.CardRepository;

import java.util.ArrayList;
import java.util.Collections; // Added import for Collections
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSession {
    private final Game game;
    private final List<GamePlayer> players;
    private final Object lock = new Object();
    private int currentPlayerIndex = 0;
    private boolean isClockwise = true;
    // For Wild Draw Four challenge
    private boolean challengeInProgress = false;
    private String challengerUsername;
    private String challengedUsername;
    // Add needed fields to track cards
    private final List<Integer> deck = new ArrayList<>(); // Card IDs in the deck
    private final Map<String, List<Card>> playerHands = new HashMap<>(); // Player username -> list of cards

    public GameSession(Game game) {
        this.game = game;
        this.players = new ArrayList<>();
    }

    public void addPlayer(GamePlayer player) {
        synchronized (lock) {
            if (players.size() < 4) {
                players.add(player);
            }
        }
    }

    public boolean isPlayerTurn(String username) {
        synchronized (lock) {
            if (currentPlayerIndex < players.size()) {
                return players.get(currentPlayerIndex).getUser().getUsername().equals(username);
            }
            return false;
        }
    }

    public void nextTurn() {
        synchronized (lock) {
            if (isClockwise) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            } else {
                currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
            }
        }
    }

    public void reverseDirection() {
        synchronized (lock) {
            isClockwise = !isClockwise;
        }
    }

    public void skipTurn() {
        synchronized (lock) {
            nextTurn();
        }
    }

    public Map<String, Object> getGameState() {
        synchronized (lock) {
            Map<String, Object> state = new HashMap<>();
            state.put("gameId", game.getGameId());
            state.put("status", game.getStatus());
            state.put("isClockwise", isClockwise);
            state.put("currentPlayer", players.get(currentPlayerIndex).getUser().getUsername());
            state.put("topCard", game.getTopCard());
            // Include other necessary game state information
            return state;
        }
    }

    // Methods for handling Wild Draw Four challenges
    public void startChallenge(String challenger, String challenged) {
        synchronized (lock) {
            challengeInProgress = true;
            challengerUsername = challenger;
            challengedUsername = challenged;
        }
    }

    public void resolveChallenge(boolean challengeSuccessful) {
        synchronized (lock) {
            challengeInProgress = false;
            // Logic for resolving the challenge
            if (challengeSuccessful) {
                // If challenge is successful, the challenged player draws cards
                GamePlayer challengedPlayer = players.stream()
                        .filter(player -> player.getUser().getUsername().equals(challengedUsername))
                        .findFirst()
                        .orElse(null);
                if (challengedPlayer != null) {
                    // Logic for drawing cards
                    // challengedPlayer.drawCards(4); // Example method to draw cards
                }
            } else {
                // If challenge fails, the challenger draws cards
                GamePlayer challengerPlayer = players.stream()
                        .filter(player -> player.getUser().getUsername().equals(challengerUsername))
                        .findFirst()
                        .orElse(null);
                if (challengerPlayer != null) {
                    // Logic for drawing cards
                    // challengerPlayer.drawCards(4); // Example method to draw cards
                }
            }
        }
    }

    public void initializeGameDeck(CardRepository cardRepository) {
        synchronized (lock) {
            // Clear the deck
            deck.clear();

            // Get all cards from the database
            List<Card> allCards = cardRepository.findAll();
            if (allCards.isEmpty()) { // <- quick sanity check
                throw new IllegalStateException("Card table is empty – seed it first");
            }

            // Shuffle the cards (convert to list of IDs)
            List<Integer> allCardIds = allCards.stream().map(Card::getId).toList();
            List<Integer> shuffledIds = new ArrayList<>(allCardIds);
            java.util.Collections.shuffle(shuffledIds);

            // Add to our deck
            deck.addAll(shuffledIds);

            System.out.println("[GameSession] Initialized deck with " + deck.size() + " cards");
        }
    }

    public void dealInitialCardsToPlayer(GamePlayer player, CardRepository cardRepository) {
        synchronized (lock) {
            String username = player.getUser().getUsername();

            // Initialize player's hand if needed
            if (!playerHands.containsKey(username)) {
                playerHands.put(username, new ArrayList<>());
            }

            // Deal 7 cards to the player
            for (int i = 0; i < 7; i++) {
                if (deck.isEmpty()) { // Check if deck is empty before drawing
                    // This case should ideally be prevented by the initial deck size check
                    // and proper game logic (e.g., reshuffling discard pile if deck runs out)
                    System.err.println("[GameSession] Deck is empty while dealing initial cards to " + username);
                    break;
                }
                Card card = drawCardFromDeck(cardRepository);
                playerHands.get(username).add(card);
            }

            System.out.println("[GameSession] Dealt " + playerHands.get(username).size() +
                " cards to player " + username);
        }
    }

    public Card drawCardFromDeck(CardRepository cardRepository) {
        synchronized (lock) {
            if (deck.isEmpty()) {
                // Consider reshuffling discard pile here if applicable to your game rules
                throw new IllegalStateException("No cards left in the deck");
            }

            // Draw from the top
            Integer cardId = deck.remove(0);
            return cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalStateException("Card not found: " + cardId));
        }
    }

    public Map<String, Object> getPlayerHands() {
        synchronized (lock) {
            // Create a safe copy with only the information clients should see
            Map<String, Object> result = new HashMap<>();

            // For each player, add their cards
            for (String username : playerHands.keySet()) {
                List<Map<String, Object>> cards = new ArrayList<>();

                // Convert each card to a map of properties
                for (Card card : playerHands.get(username)) {
                    Map<String, Object> cardMap = new HashMap<>();
                    cardMap.put("id", card.getId());
                    cardMap.put("color", card.getColor().toString());
                    cardMap.put("value", card.getNumber());
                    cardMap.put("type", card.getCardType().toString());
                    cards.add(cardMap);
                }

                result.put(username, cards);
            }

            return result;
        }
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }
}

