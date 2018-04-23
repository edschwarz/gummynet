# gummynet
Neural Network / Machine Learning for a simplified version of Gin Rummy using deeplearning4j

Simplified version of Gin Rummy for Machine Learning
- 2 players, standard card deck
- Setup:
   - Deck is shuffled 
   - Each player is dealt 7 cards
   - the next undealt card on the deck is "tuned over" - exposed
- Play:
   - Players take turns 
   - start by selecting a card to add to their 7-card hand; either
       - the exposed card, or
       - the next undealt/unexposed card in the deck
   - after selecting this card, the player will have 8 cards
   - they select one to discard, replacing the exposed card with the discard
   - the formerly-exposed card is now retired from play
- Objective:
   - cards must be grouped by matching rank, or sequence-within-suit
       - e.g., three Jacks, or 4-5-6-7 of Spades, or four sevens, or 9-10-Jack-Queen of Clubs
   - a Player wins if, after the discard, the 7 cards in their hand form two groups: 
       - one of length 3, and the other of length 4
       - a card cannot be in two groups in the winning hand, so all 7 cards are in one of these groups
       - when this is achieved, the player has "gin" and wins the hand
   - if the entire deck is exhausted (all cards have been picked) but no player has won, the hand is a draw


