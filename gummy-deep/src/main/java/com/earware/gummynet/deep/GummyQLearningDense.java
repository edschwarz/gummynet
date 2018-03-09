package com.earware.gummynet.deep;

import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.util.DataManager;

public class GummyQLearningDense<O extends Encodable> extends CancellableQLearningDiscreteDense<O> {

	GummyQLearningDense(MDP<O, Integer, DiscreteSpace> mdp, IDQN<?> dqn, 
			QLearning.QLConfiguration cfg, DataManager manager) { 
		super(mdp, dqn, cfg, manager);
		// TODO Auto-generated constructor stub
	}

}
