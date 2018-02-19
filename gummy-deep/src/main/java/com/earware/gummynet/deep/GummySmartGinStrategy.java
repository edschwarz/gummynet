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

    public static double evaluateHistogramFit(double[] histogram) {
   		double fitness = 0;
    		double[] referenceHistogram = GummySmartGinStrategy.HISTOGRAM;
    		
    		// normalize input
    		double[] normalizedHistogram = new double[histogram.length];
    		double sum=0;
    		for (double raw : histogram) {
    			sum+=raw;
    		}
    		
    		if (sum==0) {
    			// no wins
    			return -1; 
    		}
    		
    		for (int i=0; i<histogram.length; i++) {
    			normalizedHistogram[i] = sum!=0 ? 
    					histogram[i]/sum
    					:1;
    		}
    		
    		// sum of squares
    		for (int i=0; i<normalizedHistogram.length && i<referenceHistogram.length; i++) {
    			fitness += Math.pow((normalizedHistogram[i]-referenceHistogram[i]),2);
    		}
    		fitness = Math.pow(fitness, 0.5);  // sqrt
    		fitness = Math.pow(fitness, -1.0);  // invert
    		return fitness;
    }
    
	/**
	 * frequency counts by [card2Int] 
	 * used to measure "fit" of models in evolution
	 */
	public static final double[] HISTOGRAM = {
			0.0175970942, 0.0168848506, 0.0185262727, 0.0203893962, 0.0202532652, 0.0203257605, 0.0203352748, 0.0202859158, 0.0200333582, 0.0202128422, 0.0199715201, 0.0183111805, 0.0168840251, 0.0176044347, 0.0171402860, 0.0186810982, 0.0203618599, 0.0202095151, 0.0202291640, 0.0202916327, 0.0204079199, 0.0201611315, 0.0201967364, 0.0202146190, 0.0183993983, 0.0168636481, 0.0176043614, 0.0168757196, 0.0185866199, 0.0203260601, 0.0202086686, 0.0201247533, 0.0201351456, 0.0201333688, 0.0201483632, 0.0201605427, 0.0200115041, 0.0180736801, 0.0166807708, 0.0177736554, 0.0169225738, 0.0185345540, 0.0203046659, 0.0200956980, 0.0199879602, 0.0201509309, 0.0202469177, 0.0203708693, 0.0204073312, 0.0200580900, 0.0183925039, 0.0169124916
			};
}
