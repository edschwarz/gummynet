package com.earware.gummynet.deep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.Learning;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import com.earware.gummynet.gin.*;

public class GummyDeepGinStrategy implements GinStrategy {
	DQN<?> dqn;
	GummyState gummyState;
	public Map<Integer,Integer> decisionMap = new HashMap<Integer,Integer>();
	public GummySimulatorStats stats = new GummySimulatorStats();
	
	GummyDeepGinStrategy(String dqnPath) {
        dqn = loadDQN(dqnPath);
        gummyState = new GummyState(new GinHand(new Player("a"), new Player("b")));
	}
	
	GummyDeepGinStrategy(String dqnPath, GummySimulatorStats stats) {
		this(dqnPath);
        this.stats = stats;
	}
	
	@Override
	public Move.Draw.Source getDrawSource(GinHand hand) {
		if (!hand.equals(gummyState.getGinHand())) {
	        gummyState = new GummyState(hand);			
		}
		int decision = getDecision(gummyState);
		stats.addDraw(decision);
		return (decision == 0 ? Move.Draw.Source.PILE : Move.Draw.Source.DECK); 
	}
	
	@Override
	public Card getDiscardCard(PlayerHand playerHand, GinHand hand) {
		if (!hand.equals(gummyState.getGinHand())) {
	        gummyState = new GummyState(hand);			
		}
		Card discard = playerHand.getCards().get(0);
        GummyState stateCopy = new GummyState(hand);
		List<Card> candidates = new ArrayList<Card>();
		for (int i=0; i<playerHand.getCards().size(); i++) {
	        double[] values = stateCopy.toArray();
			values[Deck.card2Int(hand.getDiscard())] = new GummyState.InputWeights().discardPile;
			values[Deck.card2Int(playerHand.getCards().get(i))] = new GummyState.InputWeights().topOfPile;
			int decision = getDecision(values);
			stats.addDiscardVote(decision);
			if (decision == 1) {  // 1=="deck" meaning we did not want the card
				candidates.add(playerHand.getCards().get(i));
			}
		}
		int choice = -1;
		if (candidates.size()>0) {
			double d = ((double)candidates.size())*Math.random();
			choice = (int)d;
			discard=candidates.get(choice);
		} else {
			double d = ((double)playerHand.getCards().size())*Math.random();
			choice = (int)d;
			discard=playerHand.getCards().get(choice);
		}
		stats.addDiscard(choice);
		return discard;
	}
	
	protected int getDecision(GummyState obs) {
		return getDecision(obs.toArray());
    }
	
	protected int getDecision(double[] values) {
		
		INDArray hstack;
		if (GummyState.isConvolution()) {
			int[] shape= {13,4};
			INDArray input = Nd4j.create(values, shape);
			INDArray[] history = new INDArray[] {input};
	        hstack = Nd4j.concat(0, history); 
	        hstack = hstack.reshape(Learning.makeShape(1, hstack.shape()));
	        hstack = hstack.reshape(Learning.makeShape(1, hstack.shape()));
		} else {
			INDArray input = Nd4j.create(values);
			INDArray[] history = new INDArray[] {input};		
			hstack = Nd4j.concat(0, history);
		}

        INDArray qs = dqn.output(hstack);
        int maxAction = Learning.getMaxAction(qs);
        Double maxQ = qs.getDouble(maxAction);
        logDecision(maxAction);
        return maxAction;
    }
	
	protected void logDecision(int decision) {
		Integer count = decisionMap.get(decision);
		if (count==null) count = new Integer(0);
		decisionMap.put(decision, count+1);
	}
	
    @SuppressWarnings("rawtypes")
	private static DQN<?> loadDQN(String path) {
    		DQN<?> loadedDQN = null;
    		MultiLayerNetwork mln = null;
    		try {
    			mln = ModelSerializer.restoreMultiLayerNetwork(path, true);
    		} catch (IOException e) {
    			LOGGER.severe("Cannot load DQN at path \"" + path + "\": " + e.getMessage());
    			throw new IllegalArgumentException("IOException trying to load DQN",e); 
		}
    		loadedDQN = new DQN(mln);
    		return loadedDQN;
    }
    
    public static Logger LOGGER = Logger.getLogger(GummyDeepGinStrategy.class.getName()); 
    // static {try {Handler handler=new FileHandler("logs/PlayDeepGin.log"); handler.setFormatter(new SimpleFormatter()); LOGGER.addHandler(handler);} catch (Exception e) {e.printStackTrace();}}
}
