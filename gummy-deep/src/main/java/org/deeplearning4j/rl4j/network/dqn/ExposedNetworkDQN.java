package org.deeplearning4j.rl4j.network.dqn;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.nn.api.NeuralNetwork;

public class ExposedNetworkDQN<NN extends DQN<NN>> extends DQN<NN> {

	public ExposedNetworkDQN(MultiLayerNetwork mln) {
		super(mln);
	}
	
	public ExposedNetworkDQN(DQN<?> dqn) {
		super(dqn.mln);
	}

	public MultiLayerNetwork getMln() {
		return (MultiLayerNetwork)getNeuralNetworks()[0];
	}
	
	/**
	 * this is in deeplearning 0.9.2, I guess
	 * @return
	 */
	public NeuralNetwork[] getNeuralNetworks() {
		return new NeuralNetwork[] { mln };
	}
}
