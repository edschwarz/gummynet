package com.earware.gummynet.deep;

import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.util.DataManager;

public class GummyQLearningConv<O extends Encodable> extends CancellableQLearningDiscreteConv<O> {

	GummyQLearningConv(MDP<O, Integer, DiscreteSpace> mdp, IDQN<?> dqn, QLConfiguration cfg, DataManager manager) {
		super(mdp, dqn, cfg, manager);
		// TODO Auto-generated constructor stub
	}

}
