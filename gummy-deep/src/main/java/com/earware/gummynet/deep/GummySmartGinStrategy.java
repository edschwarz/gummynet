package com.earware.gummynet.deep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.earware.gummynet.gin.Card;
import com.earware.gummynet.gin.GinHand;
import com.earware.gummynet.gin.GinStrategy;
import com.earware.gummynet.gin.Move;
import com.earware.gummynet.gin.PlayerHand;

public class GummySmartGinStrategy implements GinStrategy {
	GummyMDP.Rewards rewards;
	
	public GummySmartGinStrategy(GummyMDP.Rewards rewards) {
		this.rewards = rewards;
	}
	
	@Override
	public Move.Draw.Source getDrawSource(GinHand hand) {
		PlayerHand lookAheadHand = hand.getCurrentPlayerHand().copy();
		double preDrawScore = GummyMDP.score(lookAheadHand, rewards);
		lookAheadHand.getCards().add(hand.getDiscard());
		double postDrawScore = GummyMDP.score(lookAheadHand, rewards);
		if (postDrawScore>preDrawScore) {
			return Move.Draw.Source.PILE;
		} else {
			return Move.Draw.Source.DECK;
		}
	}
	
	@Override
	public Card getDiscardCard(PlayerHand playerHand, GinHand hand) {
		PlayerHand currentHand = hand.getCurrentPlayerHand().copy();
		double preDiscardScore = GummyMDP.score(currentHand, rewards);
		List<Card> candidates = new ArrayList<Card>();
		List<Double> deltas = new ArrayList<Double>();
		for (int i=0; i<currentHand.getCards().size(); i++) {
			PlayerHand lookAheadHand = hand.getCurrentPlayerHand().copy();
			lookAheadHand.getCards().remove(i);
			double postDiscardScore = GummyMDP.score(lookAheadHand, rewards);
			deltas.add(postDiscardScore - preDiscardScore);
			if (postDiscardScore>=preDiscardScore) {
				// it didn't hurt - it's a candidate
				candidates.add(currentHand.getCards().get(i));
			}
		}
		if (candidates.size()>0) {
			Collections.shuffle(candidates);
			return candidates.get(0);
		} else {
			double max=-10000000000000.0;
			int index=-1;
			for (int i=0; i<deltas.size(); i++) {
				double delta = deltas.get(i);
				if (delta>max) {
					max=delta;
					index=i;
				}
			}
			return currentHand.getCards().get(index);
			// return currentHand.getCards().get((int)(Math.random()*currentHand.getCards().size()));
		}
	}		
}
