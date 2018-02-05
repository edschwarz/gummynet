package com.earware.gummynet.deep;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.json.JSONObject;

import com.earware.gummynet.gin.*;
import com.earware.gummynet.gin.Move.Draw;

import java.util.*;

public class GummyComboMDP extends GummyMDP {

    final private static int ACTION_SIZE = 2; 
    final private static int DISCARD_ACTION_SIZE = 8; 
    final private static int ACTION_DRAW_PILE = 0; // draw from PILE
    final private static int ACTION_DRAW_DECK = 1; // draw from DECK
    
	private DiscreteSpace actionSpace = new DiscreteSpace(ACTION_SIZE);
    private ObservationSpace<GummyState> observationSpace 
    				= new ArrayObservationSpace<GummyState>(new int[] {GummyState.STATE_SIZE});
    DiscardGummyState discardingState = null;
    Integer[] votes = new Integer[DISCARD_ACTION_SIZE];

    //----------------------------------------------

    public GummyComboMDP() {
    }
      
    public GummyComboMDP newInstance() {
        return new GummyComboMDP();
    }
    
    public GummyComboMDP(GummySimulator gummySimulator, Rewards rewards) {
    		super(gummySimulator, rewards);
    }
    
    @Override
    public StepReply<GummyState> step(Integer a) {
//LOGGER.info("*******>>>  " + gummyState().getGinHand().getCurrentPlayer().name + " " + gummyState().getGinHand().getPendingAction() + "  decision: " + a + "   discardingState: " + discardingState );    	
gummyState().toArray(); // force this since acutal learning is evidently done on copies :-(    	
    	
    		gummyState().stepCount++;      		
        double reward = 0;
    
		if (discardingState!=null && discardingState.isDiscarding) {
			// record the "vote" on the current card in the hand
			int voteIndex = discardingState.inMyHandIndex;
			votes[voteIndex]=a;
			PlayerHand discardingHand = discardingState.getGinHand().getCurrentPlayerHand();
			int handSize = discardingHand.getCards().size(); 
			double oldScore = this.score(discardingHand);
			List<Card> voteHandCards = new ArrayList<Card>(discardingHand.getCards());
			voteHandCards.remove(voteIndex);
			
			reward += calculateDiscardingReward(a, oldScore, new PlayerHand(voteHandCards), rewards);
//LOGGER.info(
//discardingState.getGinHand().getCurrentPlayer().name		  
//+ " NESTED DISPATCH --- decision was: " + a 
//+ " (" + discardingHand.getCards().get(voteIndex)+ "/" + (voteIndex) + ")" 
//+ " from hand: " + discardingHand
//+ " and got: " + reward
//+ "  @" + gummyState().toString(reward));        		
			if (discardingState.inMyHandIndex<handSize-1) {
				discardingState.inMyHandIndex++;
				gummyState().setCurrentHandReward(gummyState().getCurrentHandReward()+reward);				
				return new StepReply<GummyState>(discardingState, reward, isDone(), new JSONObject("{}"));
			}
			// falls through if discarding is complete
			// will handle as discard
		}
		
        GinHand ginHand = gummyState().getGinHand();
        if (ginHand.getPendingAction().equals(GinHand.ActionType.DRAW)) {
        		return dealWithDraw(a,ginHand);
        } else if (ginHand.getPendingAction().equals(GinHand.ActionType.DISCARD)) {
        		return dealWithDiscard(a);
        } else {
        		throw new IllegalArgumentException(a + " is not a valid action for " + this.getClass().getName());
        }
        
    }
    
    public StepReply<GummyState> dealWithDiscard(Integer a) {

    		double reward = 0;
		double oldScore = scoreCurrentHand();
		GinHand ginHand = gummyState().getGinHand();
		Card targetDiscard;
		
		if (discardingState == null || !discardingState.isDiscarding) {
			// not yet discarding
			// start the discarding sequence
			GinHand discardingGinHand = ginHand.copy();
			discardingState = new DiscardGummyState(discardingGinHand);
			discardingState.isDiscarding = true;
//LOGGER.info(
//ginHand.getCurrentPlayer().name		  
//+ " DISCARD BEGINS +++ decision was: " + a + " (8 cards, no TOP_OF_PILE)" 
//					+ " from hand: " + ginHand.getCurrentPlayerHand()
//					+ " and got: " + reward
//					+ "  @" + gummyState().toString(reward));        		
			return new StepReply<GummyState>(discardingState, reward, isDone(), new JSONObject("{}"));
		}
		
		// if we made it here, we're done discarding
		if (discardingState!=null) {
			GinHand discardingGinHand = discardingState.getGinHand();
			int voteIndex = discardingState.inMyHandIndex;
//LOGGER.info(
//discardingState.getGinHand().getCurrentPlayer().name		  
//+ " NESTED RETURN +++ decision was: " + a 
//+ " (" + discardingGinHand.getCurrentPlayerHand().getCards().get(voteIndex)+ "/" + (voteIndex) + ")" 
//+ " from hand: " + discardingGinHand.getCurrentPlayerHand()
//+ " and got: " + reward + " (will not be used)"
//+ "  @" + gummyState().toString(reward));        		
		}
		discardingState = null;
		int handIndex = selectDiscardFromHand();
		stats().addDiscard(handIndex);
		targetDiscard = ginHand.getCurrentPlayerHand().getCards().get(handIndex);
	    		
		//LOGGER.info("Discard " + targetDiscard.toString() + "  @" + gummyState().toString(reward));
		//LOGGER.info(">>>>>   chose from " +  ginHand.getCurrentPlayerHand().toString());
		String myName = ginHand.getCurrentPlayer().getName();		
		ginHand.pushDiscard(targetDiscard);  // WARNING: flips current player!
		PlayerHand myHand = ginHand.getPlayerHand(myName); // need to grab back "myHand"
        
		// per-step reward based on current hand score
		reward += calculateHandReward(oldScore,myHand,rewards);	        
        
        // if old and new scores are equal, get a reward for 
        // avoiding bad play. Otherwise there is no way to
        // get good feedback for Discard
        if (score(myHand)==oldScore) {
        		reward += oldScore*rewards.discardStatusQuoPerPointReward;
        }
        
		gummyState().setNoncurrentHandReward(gummyState().getNoncurrentHandReward()+reward);				
		GummyState returnState = gummyState().copy();
		
		reportDiscard(myName, reward);
//LOGGER.info(
//myName  
//+ " discarded " + targetDiscard
//+ " from hand: " + myHand
//+ " and got: " + reward
//+ "  @" + gummyState().toString(reward));        		

 		gummySimulator.latch(this);
		
		if (isDone()) {
			// the game ended while the other player was playing
			GinHand endingGinHand = gummyState().getGinHand();
			Player endingPlayer = endingGinHand.getCurrentPlayer();
			if (endingGinHand.getCurrentPlayerHand().wins()) {
				if (!endingPlayer.equals(endingGinHand.getPlayer(myName))) {
					// we lost. Pay the price
					reward -= rewards.lossPenalty;
					returnState.setNoncurrentHandReward(returnState.getNoncurrentHandReward()-rewards.lossPenalty);				
LOGGER.info(" alas, "+ myName +" lost...  my handReward is now " + returnState.getNoncurrentHandReward());				
				}
			} else if (endingGinHand.isDeckEmpty()) {
				// the game ran too long, it's everybody's fault
				reward -= rewards.flunkPenalty;
				returnState.setNoncurrentHandReward(returnState.getNoncurrentHandReward()-rewards.flunkPenalty);				
LOGGER.info(" "+ myName +" blew it again.  my handReward is now " + returnState.getNoncurrentHandReward());				
			}
//LOGGER.info(
//myName  
//+ " POST_LATCH dealWithDiscard-isDone()=true on " + targetDiscard
//+ " from hand: " + myHand
//+ " and got: " + reward
//+ "  @" + gummyState().toString(reward));        		
		}
		
		return new StepReply<GummyState>(returnState, reward, isDone(), new JSONObject("{}"));
    }
    
    private int selectDiscardFromHand() {
    		int handIndex = -1;
    		List<Integer> candidates = new ArrayList<Integer>();
    		for (int i=0; i<votes.length; i++) {
    			Integer vote = votes[i]; 
    			if (vote!=null) {
        			stats().addDiscardVote(vote);
    				if (vote.equals(ACTION_DRAW_DECK)) {
    					// it was rejected for pickup, so it is a candidate
    					candidates.add(i);
    				}
    			}
    		}
    		if (candidates.isEmpty()) {
    			// everyone was wanted, seems
    			// unlikely, but just boot
    			handIndex = (int)(Math.random()*DISCARD_ACTION_SIZE);
    		} else {
    			handIndex = candidates.get((int)(Math.random()*candidates.size())); 
    		}
    		return handIndex;
    }
    
    private StepReply<GummyState> dealWithDraw(Integer a, GinHand ginHand) {
    		double reward = 0;
    		double oldScore = score(ginHand.getCurrentPlayerHand());
    		stats().addDraw(a);
		// LOGGER.info("Draw from " + drawSourceFromAction(a) + "  @" + gummyState().toString(reward));        		
        	ginHand.pushDraw(drawSourceFromAction(a));
        	Collections.sort(ginHand.getCurrentPlayerHand().getCards());
    			
        // per-step reward based on current hand score
    		reward += calculateDrawReward(a, oldScore,ginHand.getCurrentPlayerHand(),rewards);
    		
        	if (ginHand.getCurrentPlayerHand().wins()) {
    			reward += rewards.winReward;
LOGGER.info(" "+ gummyState().getGinHand().getCurrentPlayer().getName() + " is the winner!! my handReward is now " + (gummyState().getCurrentHandReward() + reward));				
    		} else if (ginHand.isDeckEmpty()) {
    			reward -= rewards.flunkPenalty;
LOGGER.info(" "+ gummyState().getGinHand().getCurrentPlayer().getName() + " drew last card. my handReward is now " + (gummyState().getCurrentHandReward() + reward));				
    		} else if (shouldDrain(a,ginHand)) {
LOGGER.info( "pfft>>> draining " + ginHand.getDeck().getUndealt().size());
    			ginHand.drain();
    			reward -= rewards.flunkPenalty;
LOGGER.info(" " + gummyState().getGinHand().getCurrentPlayer().getName() + " must DRAIN!  my handReward is now " + (gummyState().getCurrentHandReward() + reward));				
    		}
		        		
		gummyState().setCurrentHandReward(gummyState().getCurrentHandReward() + reward);
		GummyState returnState = gummyState().copy();
		
		reportDraw(returnState.getGinHand().getCurrentPlayer().getName(), reward);
//LOGGER.info(
//returnState.getGinHand().getCurrentPlayer().getName()  
//+ " Drew from " + drawSourceFromAction(a)
//+ " to hand: " + returnState.getGinHand().getCurrentPlayerHand()
//+ " and got: " + reward
//+ "  @" + gummyState().toString(reward));        		
		if (isDone()) {
			reportDone();
			gummySimulator.latchDone(this);
			return new StepReply<GummyState>(returnState, reward, true, new JSONObject("{}"));
		} else { 
			// gummySimulator.latch(this); -- no latch in comb0, just go discard
			return new StepReply<GummyState>(returnState, reward, isDone(), new JSONObject("{}"));
		}

    }
    
    protected static double calculateDrawReward(int action,
    										double beforeScore,
    							            PlayerHand afterHand,
    							            Rewards rwds) {
    	
    		double reward = calculateHandReward(beforeScore,afterHand,rwds);
    		double afterScore = score(afterHand, rwds);
    		if (!(afterScore>beforeScore) && action == ACTION_DRAW_PILE) {
			// drew from the pile when it was not helpful
			reward -= rwds.pointlessDrawPenalty;
		}
    		return reward;
    }

    protected static double calculateDiscardingReward(int action,
			double beforeScore,
            PlayerHand afterHand, // the hand after discarding
            Rewards rwds) {
		double reward = 0; 
		double afterScore = score(afterHand, rwds);
		double handReward = (afterScore-beforeScore) * rwds.handScoreDeltaPerPointReward/DISCARD_ACTION_SIZE;
		
		// because beforeScore is an 8-card hand
		// the score can never improve
		
		if (afterScore<beforeScore) {
			// hand will be degraded if this card is discarded
			// so we want to keep it
			if (action == ACTION_DRAW_PILE) {  // recommended for keep
				reward -= handReward; // reverse to reward for accurately identifying
			} else {
				reward += handReward; // "rejected" for keep (by selecting DECK). assess penalty, we wanted this card 
			}
		} else {
			// hand will be kept the same if this card is discarded
			// should be a candidate for discard
			if (action == ACTION_DRAW_PILE) {  // recommended for keep
				reward -= rwds.pointlessKeepPenalty;   
			} else {
				reward += rwds.goodDiscardRecommendationReward; // good job! 
			}
    		}
 		return reward;
    }
    
    //-----------------------------------------
    private static class DiscardGummyState extends GummyState {
    		DiscardGummyState(GinHand hand) {
    			super(hand);
    		}
    		
    		public boolean isDiscarding = true;
    		public int inMyHandIndex = 0;
    		
    		@Override 
    		public double[] toArray() {
    			double[] values = super.toArray();
    			// make the inMyHand card at [inMyHandIndex] appear to be on offer for DRAW
    			int ndx=inMyHandIndex;
    			Card card = this.getGinHand().getCurrentPlayerHand().getCards().get(ndx);
    			if (card!=null) {
    				//LOGGER.info("fixing up card: " + card + "  from hand: " + this.getGinHand().getCurrentPlayerHand());    				
    				values[Deck.card2Int(card)] = getInputWeights().topOfPile;
    			}     			
    			return values;
    		}
    }
    
    //-----------------------------------------
    protected Draw.Source drawSourceFromAction(int a) {
		if (a==GummyComboMDP.ACTION_DRAW_DECK) {
			return Draw.Source.DECK;
		} else if (a==GummyComboMDP.ACTION_DRAW_PILE) {
			return Draw.Source.PILE;
		}
		throw new IllegalArgumentException(a + " is not a valid choice for draw source action");
    }
    
    private void reportDraw(String name, Double reward) {
    		addReward(name, reward, gummyState().drawRewards);
    		addReward(name, reward, gummyState().netRewards);
    }
    private void reportDiscard(String name, Double reward) {
		addReward(name, reward, gummyState().discardRewards);
		addReward(name, reward, gummyState().netRewards);
    }
    private void addReward(String name, Double reward, Map<String,Double> map) {
    		Double d = map.get(name);
    		if (d==null) {
    			d=0.0;
    		}
    		map.put(name, d+reward);
    }
    
    @Override
    public DiscreteSpace getActionSpace() {return actionSpace;}
    
    @Override
    public ObservationSpace<GummyState> getObservationSpace() {return observationSpace;}
}

