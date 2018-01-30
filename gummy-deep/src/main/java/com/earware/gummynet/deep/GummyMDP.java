package com.earware.gummynet.deep;

import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.earware.gummynet.gin.*;

import java.util.*;
import java.util.logging.*;

public abstract class GummyMDP implements MDP<GummyState, Integer, DiscreteSpace> {

    final public static int MAX_STEPS_PER_GAME = 1000;
    
    //----------------------------------------------
    protected GummySimulator gummySimulator=null;
    protected Rewards rewards = new Rewards(); 

    //----------------------------------------------
    public GummyMDP() {
    }
        
    public GummyMDP(GummySimulator gummySimulator) {
    		this.gummySimulator = gummySimulator;
    }
            
    public GummyMDP(GummySimulator gummySimulator, Rewards rewards) {
		this.gummySimulator = gummySimulator;
		this.rewards = rewards;
    }
    
    public Rewards getRewards() {return rewards;}
	public void setRewards(Rewards rewards) {this.rewards = rewards;}
	public GummySimulator getGummySimulator() {return this.gummySimulator;}
	public GummyState gummyState() {return gummySimulator.getGummyState();}
	public boolean needsLatch() {return true;}
	
    @Override
    public boolean isDone() {
		if (gummyState() == null || gummyState().getGinHand() == null) {
			LOGGER.warning("initialization problem? gummyState() is null in GummyMDP");
			return true;	
		}
    		return isDone(gummyState());
    }
    
    protected boolean isDone(GummyState state) {
    		GinHand ginHand = state.getGinHand();
    		if (ginHand.isDeckEmpty() || ginHand.getCurrentPlayerHand().wins()) {
    			//LOGGER.info(" -->  isDone()=true : " + this.getClass().getSimpleName() + "  @" + state.toString());
    			return true;
    		}
    		return false;
    }

    @Override
    public GummyState reset() {
    		// LOGGER.info(" -->  reset()=true : " + this.getClass().getSimpleName() + "  @" + gummyState().toString());
    		stats().addReset();
    	    	if (gummySimulator.getStats().getNumHandsCompleted()>0) { 
    	    		gummySimulator.reset(this);
    	    	}
    		return gummyState();
    }
    
    static double calculateHandReward(double oldScore, PlayerHand playerHand, Rewards rwds) {
    		double score = 0;
		double afterScore = score(playerHand,rwds);
		score +=  afterScore * rwds.handScoreKeepalivePerPointReward;
        double net = afterScore - oldScore; 
		if (net!=0) {
			score +=  net * rwds.handScoreDeltaPerPointReward;
			//LOGGER.info(" net: " +  net + "  @" + gummyState().toString(reward));
		}
		return score;
    }
    
    protected double scoreCurrentHand() {
		return score(gummyState().getGinHand().getCurrentPlayerHand());
    }
    
	protected double score(PlayerHand hand) {
		return score(hand, rewards);
	}
	protected static double score(PlayerHand hand, Rewards rwds) {
		double score=0;
		List<List<Card>> matches = GinTester.findMatches(hand,2,4);
		for (List<Card> list : matches) {
			score += scoreCardList(list, rwds);
		}
		List<List<Card>> runs = GinTester.findRuns(hand,2,4);
		for (List<Card> list : runs) {
			score += scoreCardList(list, rwds);
		}
		return score;
	}
	
	static double scoreCardList(List<Card> list, Rewards rwds) {
		double score = 0;
		score += list.size();
		if (list.size()>2) {
			score += rwds.handScore3Bonus;
		}
		if (list.size()>3) {
			score += rwds.handScore4Bonus;
		}
		return score;
	}
	
    protected boolean shouldDrain(Integer a, GinHand ginHand) {
		//if (ginHand.getDeck().getUndealt().size()<3	 && a.equals(ACTION_DRAW_PILE)) {
		//	return true;
		//} 
		if (gummyState().stepCount > MAX_STEPS_PER_GAME) {
			return true;
		}
		return false;
    }

    protected void reportDone() {
		GinHand ginHand = gummyState().getGinHand();
		if (ginHand.getCurrentPlayerHand().wins()) {
			stats().addWin(gummyState());
			LOGGER.info("!! WINNER !! " 
					+ GinTester.asWinningHand(ginHand.getCurrentPlayerHand()).toString() 
					+ "  @" + gummyState().toString() 
					);
			LOGGER.info("!! WINNER !! " 
					+ ginHand.toString());
			
		} else {
			if (gummyState().stepCount > MAX_STEPS_PER_GAME) {
				stats().addDrain(gummyState());
					LOGGER.info("NO WINNER - DRAIN" + "  @" + gummyState().toString());
			} else {
				stats().addFlunk(gummyState());
				LOGGER.info("NO WINNER " + "  @" + gummyState().toString());
			}
		}
    }

    protected GummySimulatorStats stats() {return gummySimulator.getStats();}
        
    @Override
    public void close() {}
    
    //-----------------------------------------
    public static class Rewards {
		// rewards for goodness play
        double winReward = 300.0;
        double lossPenalty = 50.0;
		double flunkPenalty = 200.0; 
        double handScoreDeltaPerPointReward = 2.0;  // for every "point" CHANGE (up or down) in GinTester.score(myHand) 
		double pointlessDrawPenalty=30; // drawing when it doesn't help
		double pointlessKeepPenalty=4; // reccomending keep when it doesn't help
        double handScoreKeepalivePerPointReward = 0;  // for every "point" in GinTester.score(myHand), each hand 
        double discardStatusQuoPerPointReward = 0;  // Discarder gets a reward for preserving 
		double goodDiscardRecommendationReward=0; // keeping when it doesn't help

		double handScore3Bonus=5;  // used in scoring, not directly a reward
		double handScore4Bonus=10;  // used in scoring, not directly a reward

    		public double getPointlessKeepPenalty() {return pointlessKeepPenalty;}
		public void setPointlessKeepPenalty(double pointlessKeepPenalty) {this.pointlessKeepPenalty = pointlessKeepPenalty;}
		public double getGoodDiscardRecommendationReward() {return goodDiscardRecommendationReward;}
		public void setGoodDiscardRecommendationReward(double goodDiscardRecommendationReward) {this.goodDiscardRecommendationReward = goodDiscardRecommendationReward;}
    		public double getWinReward() {return winReward;}
		public void setWinReward(double winReward) {this.winReward = winReward;}
		public double getLossPenalty() {return lossPenalty;}
		public void setLossPenalty(double lossPenalty) {this.lossPenalty = lossPenalty;}
		public double getHandScoreDeltaPerPointReward() {return handScoreDeltaPerPointReward;}
		public void setHandScoreDeltaPerPointReward(double handScoreDeltaPerPointReward) {this.handScoreDeltaPerPointReward = handScoreDeltaPerPointReward;}
		public double getHandScoreKeepalivePerPointReward() {return handScoreKeepalivePerPointReward;}
		public void setHandScoreKeepalivePerPointReward(double handScoreKeepalivePerPointReward) {this.handScoreKeepalivePerPointReward = handScoreKeepalivePerPointReward;}
		public double getDiscardStatusQuoPerPointReward() {return discardStatusQuoPerPointReward;}
		public void setDiscardStatusQuoPerPointReward(double discardStatusQuoPerPointReward) {this.discardStatusQuoPerPointReward = discardStatusQuoPerPointReward;}
		public double getHandScore3Bonus() {return handScore3Bonus;}
		public void setHandScore3Bonus(double handScore3Bonus) {this.handScore3Bonus = handScore3Bonus;}
		public double getHandScore4Bonus() {return handScore4Bonus;}
		public void setHandScore4Bonus(double handScore4Bonus) {this.handScore4Bonus = handScore4Bonus;}
		public double getPointlessDrawPenalty() {return pointlessDrawPenalty;}
		public void setPointlessDrawPenalty(double pointlessDrawPenalty) {this.pointlessDrawPenalty = pointlessDrawPenalty;}
		public double getFlunkPenalty() {return flunkPenalty;}
		public void setFlunkPenalty(double flunkPenalty) {this.flunkPenalty = flunkPenalty;}
    }
    
    //----------------------------------------------
    // test/debug support of some kind
    private static List<GummyState> _states = new ArrayList<GummyState>();  // test

    public static void printTest(@SuppressWarnings("rawtypes") IDQN idqn) {
        INDArray input = Nd4j.create(_states.size(), GummyState.STATE_SIZE);
        for (int i = 0; i < MAX_STEPS_PER_GAME; i++) {
            input.putRow(i, Nd4j.create(_states.get(i).toArray()));
        }
        INDArray output = Nd4j.max(idqn.output(input), 1);
        Logger.getAnonymousLogger().info(output.toString());
    }

    public static Logger LOGGER = Logger.getLogger(GummyMDP.class.getPackage().getName()); 
}

