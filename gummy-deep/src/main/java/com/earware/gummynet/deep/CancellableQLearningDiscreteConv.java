package com.earware.gummynet.deep;

import java.util.ArrayList;

import org.deeplearning4j.rl4j.learning.IHistoryProcessor;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteConv;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.util.DataManager;
import org.deeplearning4j.rl4j.util.DataManager.StatEntry;

public class CancellableQLearningDiscreteConv<O extends Encodable>
		extends QLearningDiscreteConv<O> {
	
	private boolean cancelled = false;
	public void cancel() {cancelled = true;}
	
	CancellableQLearningDiscreteConv(MDP<O, Integer, DiscreteSpace> mdp, IDQN<?> dqn, 
					QLearning.QLConfiguration cfg, DataManager manager) {
		super(mdp, dqn, IHistoryProcessor.Configuration.builder().rescaledHeight(13).rescaledWidth(4).croppingHeight(13).croppingWidth(4).historyLength(1).skipFrame(1).build(), cfg, manager);
	}
	
	@Override
    protected StatEntry trainEpoch() {
		if (!cancelled) {
			try {
				return super.trainEpoch();
			} catch (RuntimeException e) {
				if (cancelled) {
					// if we were cancelled by another thread?
					System.err.println("CANCELLED EXCEPTION: " + e.getMessage());
				} else {
					throw(e);
				}
			}
		}
		while (getStepCounter()<getConfiguration().getMaxStep()) {
			incrementStep();
		}
		double reward = 0;
		int step = 0;
		StatEntry statEntry = new QLStatEntry(getStepCounter(), getEpochCounter(), reward, step, new ArrayList<Double>(),
                getEgPolicy().getEpsilon(), 0.0000001, 0.0000001);
		return statEntry;
	}
}

