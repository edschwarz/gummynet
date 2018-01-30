package com.earware.gummynet.deep;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;

import com.earware.gummynet.gin.Card;
import com.earware.gummynet.gin.GinStrategy;
import com.earware.gummynet.gin.Move.Draw;

public class GummyGinStrategyMDP extends GummyMDP {
	
	GinStrategy strategy = null;
	
	GummyGinStrategyMDP(GummySimulator simulator) {
		super(simulator);
	}
	
	@Override
	public boolean needsLatch() {return false;}

	@Override
	public StepReply<GummyState> step(Integer arg0) {
		Draw.Source drawSource = strategy.getDrawSource(gummyState().getGinHand());
		gummyState().getGinHand().pushDraw(drawSource);
		
		if (isDone()) {
			reportDone();			
		} else {
			Card discard = strategy.getDiscardCard(
					gummyState().getGinHand().getCurrentPlayerHand(), 
					gummyState().getGinHand());
			gummyState().getGinHand().pushDiscard(discard);
	    }
		return null;
	}
	
	@Override
	public DiscreteSpace getActionSpace() {return null;}
	@Override
	public ObservationSpace<GummyState> getObservationSpace() {return null;}
	@Override
	public MDP<GummyState, Integer, DiscreteSpace> newInstance() {return null;}

}
