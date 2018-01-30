package com.earware.gummynet.deep;

public class GummyDeepPlayerMDP extends GummyGinStrategyMDP {
	public GummyDeepPlayerMDP(GummySimulator simulator, String dqnPath) {
		super(simulator);
		this.strategy = new GummyDeepGinStrategy(dqnPath);
	}
}
