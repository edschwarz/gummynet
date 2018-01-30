package com.earware.gummynet.deep;

import org.deeplearning4j.rl4j.space.Encodable;

import com.earware.gummynet.gin.*;
import com.earware.gummynet.gin.GinHand.ActionType;
import com.earware.gummynet.gin.Move.Draw.Source;

import java.util.*;

/**
 * The state of a single hand of gin
 *  - also some per-hand statistics for reporting
 * @author edward
 * 
 * encoding is as an array of "cards"
 * where the array index is the card identifier
 *   (via Deck.cardToInt() and vice-versa)
 * and the value is the location of the card
 *   (via inputWeights)  
 * 
 */
public class GummyState implements Encodable {

	public static final int STATE_SIZE = 53;
	private static final int DRAW_IS_PENDING_INDEX = 52;
	private static final double DRAW_IS_PENDING_ACTIVATION = 2.0;
	
	/**
	 * Used to set the INPUT ENCODING WEIGHTS 
	 */
	public static class InputWeights {
		public double inMyHand=10.0;  
		public double topOfPile=-70.0;
		public double discardPile=2;
		public double pickedUp=-6;
		public double bias=-0.2; // ignored if using auto-scale
		public double scalingFactor=1;

		public double getInMyHand() {return inMyHand;}
		public void setInMyHand(double inMyHand) {this.inMyHand = inMyHand;}
		public double getTopOfPile() {return topOfPile;}
		public void setTopOfPile(double topOfPile) {this.topOfPile = topOfPile;}
		public double getDiscardPile() {return discardPile;}
		public void setDiscardPile(double discardPile) {this.discardPile = discardPile;}
		public double getPickedUp() {return pickedUp;}
		public void setPickedUp(double pickedUp) {this.pickedUp = pickedUp;}		
		public double getScalingFactor() {return scalingFactor;}
		public void setScalingFactor(double scalingFactor) {this.scalingFactor = scalingFactor;}
		public double getBias() {return bias;}
		public void setBias(double bias) {this.bias = bias;}
	}
	private InputWeights inputWeights = new InputWeights();
	public InputWeights getInputWeights() {return inputWeights;}
	public void setInputWeights(InputWeights inputWeights) {this.inputWeights = inputWeights;}

	public static int totInstances=0;
	
	// real payload
	private GinHand ginHand;

	// reporting support
	public int originalId;
	public int instanceId;
    public double handReward = 0;
	public int stepCount=0;
	public long startTime=0;
	public long endTime=0;
	
	public int totEncodingCalls=0;
	Map<String,Double> netRewards = new HashMap<String,Double>();
	Map<String,Double> drawRewards = new HashMap<String,Double>();
	Map<String,Double> discardRewards = new HashMap<String,Double>();
	public double[] netValues = new double[10];
		
	public GummyState(GinHand ginHand) {
		totInstances++;
		this.instanceId=totInstances;
		this.originalId=totInstances; // overwritten in copy()
		this.startTime=System.currentTimeMillis();
		this.ginHand = ginHand;
		if (ginHand!=null && ginHand.getPlayerHandOne()!=null 
				&& ginHand.getPlayerHandOne().getCards()!=null) {
			Collections.sort(ginHand.getPlayerHandOne().getCards());
			Collections.sort(ginHand.getPlayerHandTwo().getCards());
		}
	}
	
	// present to the Neural Net as an array of doubles 
    public double[] toArray() {
    		double[] values = new double[STATE_SIZE];
    		for (int i=0; i<values.length; i++) {
    			values[i]=0;
    		}

    		// history/discard
    		List<Move> moves = getGinHand().getMoves();
    		for (Move move : moves) {
    			if (move.getDraw()!=null && move.getDraw().source==Source.PILE) {
    				// picked up
    				values[Deck.card2Int(move.getDraw().card)]=inputWeights.pickedUp;
    			}
    			if (move.getDiscard()!=null) {
    				values[Deck.card2Int(move.getDiscard())]=inputWeights.discardPile;
    			}
    		}
    		
    		// top of pile
    		if (getGinHand().getPendingAction().equals(ActionType.DRAW)) {
    			values[Deck.card2Int(getGinHand().getDiscard())]=inputWeights.topOfPile;
    			values[DRAW_IS_PENDING_INDEX]=DRAW_IS_PENDING_ACTIVATION;
    		} else {
        		values[Deck.card2Int(getGinHand().getDiscard())]=inputWeights.discardPile;
    		}
    		
    		// my hand
    		List<Card> cards = getGinHand().getCurrentPlayerHand().getCards();
    		for (Card card : cards) {
    			values[Deck.card2Int(card)]=inputWeights.inMyHand;
    		}
    		
    		if (inputWeights.scalingFactor!=0) {
    			applyAutoBias(values);
    			applyBiasAndScale(values);
    		}
    		
    		if (totEncodingCalls<netValues.length) {
    			netValues[totEncodingCalls]=total(values);
    		}
    		totEncodingCalls++;
    		
        return values;
    }
    
    private void applyAutoBias(double[] values) {
		// auto bias to zero
		double activation=0;
		for (double v:values) {
			activation += v;
		}
		double bias = activation / values.length;
		for (int i=0; i<values.length; i++) {
			values[i] -= bias;
		}	
    }
    
    private void applyBiasAndScale(double[] values) {
		for (int i=0; i<values.length; i++) {
			double prev=values[i];
    		values[i]=values[i]+inputWeights.bias;
			values[i]=values[i]*inputWeights.scalingFactor;
			double post=values[i];
//System.out.println(" " + prev + " " + post);
		}
    }
    
    public GummyState copy() {
    		GummyState gs = new GummyState(this.getGinHand().copy());
    		gs.originalId = this.originalId;
    		gs.inputWeights = this.inputWeights;
    		gs.stepCount = this.stepCount;
    		gs.startTime = this.startTime;
    		gs.endTime = this.endTime;
    		gs.handReward = this.handReward;
    		gs.totEncodingCalls = this.totEncodingCalls;
    		gs.netRewards.putAll(this.netRewards);
    		gs.drawRewards.putAll(this.drawRewards);
    		gs.discardRewards.putAll(this.discardRewards);
    		System.arraycopy(this.netValues, 0, gs.netValues, 0, this.netValues.length);
    		return gs;
    }
    
    public String toString() {
    		return toString(0);
    }
    
    public String toString(double stepReward) {
		return "" 
				+ originalId + ':' + getGinHand().getCurrentPlayer().getName() 
				+ '/' + stepCount 
				+ '/' + String.format("%01.2f",(handReward+stepReward))
				+ "   rewards(draw/discard/net):" + drawRewards + " " + discardRewards + " " + netRewards
				;
    }

    public Object clone() {
    		return this.copy();
    }
	public GinHand getGinHand() {
		return ginHand;
	}
	public void setGinHand(GinHand ginHand) {
		this.ginHand=ginHand;
	}
	public static double total(double[] state) {
		double totalState = 0;
		for (double d: state) {
			totalState += d;
		}
		return totalState;
	}
}