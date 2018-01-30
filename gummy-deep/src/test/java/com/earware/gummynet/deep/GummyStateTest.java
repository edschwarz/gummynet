package com.earware.gummynet.deep;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.*;

import com.earware.gummynet.gin.*;
import com.earware.gummynet.gin.Move.Draw;

public class GummyStateTest {
	
	@Test
	public void testEncodeHand() {
		GinHand ginHand = newGinHand();
		ginHand.play(20);
		GummyState state = new GummyState(ginHand);
		validateHandEncode(state);
		
		if (ginHand.getPendingAction().equals(GinHand.ActionType.DRAW)) {
			ginHand.pushDraw(Draw.Source.DECK);
		} else {
			ginHand.pushDiscard(ginHand.getCurrentPlayerHand().getCards().get(0));
		}
		validateHandEncode(state);
	}
	
	private void validateHandEncode(GummyState state) {
		GinHand ginHand = state.getGinHand();
		
		GummyState.InputWeights weights = state.getInputWeights();
		weights.scalingFactor = 0;  // disable bias and scaling
		double[] stateArray = state.toArray();
		assert(stateArray.length == GummyState.STATE_SIZE);

		PlayerHand hand = state.getGinHand().getCurrentPlayerHand(); 
		for (int i=0; i<52; i++) {
			Card card = Deck.int2Card(i);
			assertNotNull(card);
			double value = stateArray[i];
			if (value == weights.inMyHand) {
				assert(hand.contains(card));
			} else if (value == weights.topOfPile) {
				assert(ginHand.getDiscard().equals(card));
			} else if (value == weights.pickedUp) {
				assert(getNotPlayingHand(ginHand).contains(card));
			} else if (value == 0) {
				// unknown - could be in the deck, or in opponent's hand
				assert(!wasEverDiscarded(ginHand, card) && !hand.contains(card));
			} else if (value == weights.discardPile) {
			} else {
				fail("invalid weight value: " + value + " for card " + card.toString());
			}
		}
	}

	private PlayerHand getNotPlayingHand(GinHand ginHand) {
		if (ginHand.getCurrentPlayer().equals(ginHand.getPlayerOne())) {
			return ginHand.getPlayerHandTwo();
		}
		return ginHand.getPlayerHandOne();
	}
	
	private GinHand newGinHand() {
		GinHand ginHand = new GinHand(new Player("joe"),
							new PlayGin.RandomGinStrategy(),
							new Player("annie"),
							new PlayGin.RandomGinStrategy());
		ginHand.deal();
		return ginHand;
	}
	
	private boolean wasEverDiscarded(GinHand ginHand, Card card) {
		for (Move move: ginHand.getMoves()) {
			if (move.getDiscard()!=null && move.getDiscard().equals(card)) {
				return true;
			}
		}
		return false;
	}
}
