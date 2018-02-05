package com.earware.gummynet.deep;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.*;

public class GummyConfig {
	public static int STEPS_PER_HAND = 210;
	boolean useUI = false;
	
	// QLEARNING 
	private int approxHandsToPlay = 500;
	private int greedyAnnealHands = 350; // how many hands to spread the annealing across; steps down every 500 steps = 16-17 hands
	double rewardScaling = 0.001; // try to get average score to be not too big
	int hardTargetUpdate = 5000;
	float minEpsilon = 0.1f;  // keep this much random amount for exploration 
	float gamma = 0.99f;  // how much to value future rewards, 0-1 
	private float tdErrorClipping = 10.0f;
	private int greedyAnnealSteps = approxHandsToPlay*(STEPS_PER_HAND/3); 
	private int maxStepsOverallQL = approxHandsToPlay*STEPS_PER_HAND;  // actual end of run
	int maxStepsPerHandQL = GummyMDP.MAX_STEPS_PER_GAME*3;
	
	// NEURAL NETWORK
	int neuralNetNumLayers = 5;  // not including "input" (from GummyState) and "output" (from ACTION_SIZE, I guess)
	int neuralNetNodesPerLayer = 77;  // in each layer
	double neuralNetL2 = 0.05;
	double neuralNetLearningRate = 0.01;  // aka alpha
			
	// rewards for playing the game
    GummyMDP.Rewards rewards = new GummyMDP.Rewards(); 

    // input encoding weights
    GummyState.InputWeights inputWeights = new GummyState.InputWeights();

    GummyConfig() {}
	
	public boolean getUseUI() {
		return useUI;
	}

	public void setUseUI(boolean useUI) {
		this.useUI=useUI;
	}

	public int getApproxHandsToPlay() {
		return approxHandsToPlay;
	}

	public void setApproxHandsToPlay(int approxHandsToPlay) {
		double ratio = ((double)this.greedyAnnealHands)/this.approxHandsToPlay;
		this.approxHandsToPlay = approxHandsToPlay;
		this.maxStepsOverallQL = this.approxHandsToPlay*STEPS_PER_HAND;
		int greedyHandsRecommendation = (int)(this.approxHandsToPlay*ratio);
		if (greedyHandsRecommendation>0) {
			this.setGreedyAnnealHands(greedyHandsRecommendation);
		}
	}

	public int getMaxStepsPerHandQL() {
		return maxStepsPerHandQL;
	}

	public void setMaxStepsPerHandQL(int maxStepsPerHandQL) {
		this.maxStepsPerHandQL = maxStepsPerHandQL;
	}

	public int getMaxStepsOverallQL() {
		return maxStepsOverallQL;
	}

	public int getHardTargetUpdate() {
		return hardTargetUpdate;
	}

	public void setHardTargetUpdate(int hardTargetUpdate) {
		this.hardTargetUpdate = hardTargetUpdate;
	}

	public double getRewardScaling() {
		return rewardScaling;
	}

	public void setRewardScaling(double rewardScaling) {
		this.rewardScaling = rewardScaling;
	}

	public int getGreedyAnnealSteps() {
		return greedyAnnealSteps;
	}

	public float getMinEpsilon() {
		return minEpsilon;
	}

	public void setMinEpsilon(float minEpsilon) {
		this.minEpsilon = minEpsilon;
	}

	public float getGamma() {
		return gamma;
	}

	public void setGamma(float gamma) {
		this.gamma = gamma;
	}

	public int getNeuralNetNumLayers() {
		return neuralNetNumLayers;
	}

	public void setNeuralNetNumLayers(int neuralNetNumLayers) {
		this.neuralNetNumLayers = neuralNetNumLayers;
	}

	public int getNeuralNetNodesPerLayer() {
		return neuralNetNodesPerLayer;
	}

	public void setNeuralNetNodesPerLayer(int neuralNetNodesPerLayer) {
		this.neuralNetNodesPerLayer = neuralNetNodesPerLayer;
	}

	public double getNeuralNetL2() {
		return neuralNetL2;
	}

	public void setNeuralNetL2(double neuralNetL2) {
		this.neuralNetL2 = neuralNetL2;
	}

	public double getNeuralNetLearningRate() {
		return neuralNetLearningRate;
	}

	public void setNeuralNetLearningRate(double neuralNetLearningRate) {
		this.neuralNetLearningRate = neuralNetLearningRate;
	}

	public GummyMDP.Rewards getRewards() {
		return rewards;
	}

	public void setRewards(GummyMDP.Rewards rewards) {
		this.rewards = rewards;
	}

	public GummyState.InputWeights getInputWeights() {
		return inputWeights;
	}

	public void setInputWeights(GummyState.InputWeights inputWeights) {
		this.inputWeights = inputWeights;
	}

	public int getGreedyAnnealHands() {
		return greedyAnnealHands;
	}

	public void setGreedyAnnealHands(int greedyAnnealHands) {
		this.greedyAnnealHands = greedyAnnealHands;
		this.greedyAnnealSteps = this.greedyAnnealHands*STEPS_PER_HAND;
	}
	
	public float getTdErrorClipping() {return tdErrorClipping;}
	public void setTdErrorClipping(float tdErrorClipping) {this.tdErrorClipping = tdErrorClipping;}

    public String toString() {
	    String s = "approxHandsToPlay"+"="+approxHandsToPlay
	    + '\n' + "maxStepsPerHandQL"+"="+maxStepsPerHandQL 
	    + '\n' + "maxStepsOverallQL"+"="+maxStepsOverallQL 
	    + '\n' + "hardTargetUpdate"+"="+hardTargetUpdate
	    + '\n' + "rewardScaling"+"="+rewardScaling
	    + '\n' + "greedyAnnealHands"+"="+greedyAnnealHands 
	    + '\n' + "greedyAnnealSteps"+"="+greedyAnnealSteps 
	    + '\n' + "minEpsilon"+"="+minEpsilon 
	    + '\n' + "gamma"+"="+gamma 
	    + '\n' + "tdErrorClipping"+"="+tdErrorClipping 
		
	    + '\n' + "neuralNetNumLayers"+"="+neuralNetNumLayers
	    + '\n' + "neuralNetNodesPerLayer"+"="+neuralNetNodesPerLayer
	    + '\n' + "neuralNetL2"+"="+neuralNetL2
	    + '\n' + "neuralNetLearningRate"+"="+neuralNetLearningRate
	    
	    + '\n' + "rewards.winReward"+"="+rewards.winReward
	    + '\n' + "rewards.lossPenalty"+"="+rewards.lossPenalty
	    + '\n' + "rewards.handScoreDeltaPerPointReward"+"="+rewards.handScoreDeltaPerPointReward
	    + '\n' + "rewards.handScoreKeepalivePerPointReward"+"="+rewards.handScoreKeepalivePerPointReward
	    + '\n' + "rewards.handScore3Bonus"+"="+rewards.handScore3Bonus
	    + '\n' + "rewards.handScore4Bonus"+"="+rewards.handScore4Bonus
	    + '\n' + "rewards.flunkPenalty"+"="+rewards.flunkPenalty
	    + '\n' + "rewards.pointlessDrawPenalty"+"="+rewards.pointlessDrawPenalty
	    + '\n' + "rewards.pointlessKeepPenalty"+"="+rewards.pointlessKeepPenalty
	    + '\n' + "rewards.goodDiscardRecommendationReward"+"="+rewards.goodDiscardRecommendationReward
	    
	    + '\n' + "inputWeights.inMyHand"+"="+inputWeights.inMyHand
	    + '\n' + "inputWeights.topOfPile"+"="+inputWeights.topOfPile
	    + '\n' + "inputWeights.discardPile"+"="+inputWeights.discardPile
	    + '\n' + "inputWeights.pickedUp"+"="+inputWeights.pickedUp
	    + '\n' + "inputWeights.scalingFactor"+"="+inputWeights.scalingFactor
	    + '\n' + "inputWeights.bias"+"="+inputWeights.bias
	    
	    + '\n' + "useUI"+"="+useUI
	    ;
    		return s;
    }
    
    public void save(String path) throws IOException {
    		FileWriter writer = new FileWriter(path);
    		new JSONObject(this).write(writer,2,0);
    		writer.close();
    }

    public static GummyConfig fromFile(String path) throws IOException {
    		JSONObject json = new JSONObject(new JSONTokener(new FileReader(path)));
    		return GummyConfig.fromJson(json);
    }
    
    public static GummyConfig fromJson(JSONObject json) {
		GummyConfig cfg = new GummyConfig();
		cfg.setFromJson(json);
		return cfg;
    }
    public void setFromJson(JSONObject json) {
    		
	    	if (json.has("approxHandsToPlay")) {this.approxHandsToPlay = json.getInt("approxHandsToPlay");}
	    	if (json.has("maxStepsPerHandQL")) {this.maxStepsPerHandQL = json.getInt("maxStepsPerHandQL");}
	    	if (json.has("maxStepsOverallQL")) {this.maxStepsOverallQL = json.getInt("maxStepsOverallQL");}
	    	if (json.has("hardTargetUpdate")) {this.hardTargetUpdate = json.getInt("hardTargetUpdate");}
	    	if (json.has("rewardScaling")) {this.rewardScaling = json.getDouble("rewardScaling");}
	    	if (json.has("greedyAnnealSteps")) {this.greedyAnnealSteps = json.getInt("greedyAnnealSteps");} 
	    	if (json.has("greedyAnnealHands")) {this.greedyAnnealHands = json.getInt("greedyAnnealHands");} 
	    	if (json.has("minEpsilon")) {this.minEpsilon = (float)json.getDouble("minEpsilon");}
	    	if (json.has("gamma")) {this.gamma = (float)json.getDouble("gamma");}
	    	if (json.has("tdErrorClipping")) {this.tdErrorClipping = (float)json.getDouble("tdErrorClipping");}
	    	if (json.has("neuralNetNumLayers")) {this.neuralNetNumLayers = json.getInt("neuralNetNumLayers");}
	    	if (json.has("neuralNetNodesPerLayer")) {this.neuralNetNodesPerLayer = json.getInt("neuralNetNodesPerLayer");}
	    	if (json.has("neuralNetL2")) {this.neuralNetL2 = json.getDouble("neuralNetL2");}
	    	if (json.has("neuralNetLearningRate")) {this.neuralNetLearningRate = json.getDouble("neuralNetLearningRate");}
	    	if (json.has("inputWeights")) {
	    		JSONObject iw = json.getJSONObject("inputWeights");
	    		if (iw.has("inMyHand")) {this.inputWeights.inMyHand=iw.getDouble("inMyHand");}
	    		if (iw.has("discardPile")) {this.inputWeights.discardPile=iw.getDouble("discardPile");}
	    		if (iw.has("pickedUp")) {this.inputWeights.pickedUp=iw.getDouble("pickedUp");}
	    		if (iw.has("topOfPile")) {this.inputWeights.topOfPile=iw.getDouble("topOfPile");}
	    		if (iw.has("scalingFactor")) {this.inputWeights.scalingFactor=iw.getDouble("scalingFactor");}
	    		if (iw.has("bias")) {this.inputWeights.bias=iw.getDouble("bias");}
	    	}
	    	if (json.has("rewards")) {
	    		JSONObject rewards = json.getJSONObject("rewards");
		    	if (rewards.has("flunkPenalty")) {this.rewards.flunkPenalty=rewards.getDouble("flunkPenalty");}
		    	if (rewards.has("handScore3Bonus")) {this.rewards.handScore3Bonus=rewards.getDouble("handScore3Bonus");}
		    	if (rewards.has("handScore4Bonus")) {this.rewards.handScore4Bonus=rewards.getDouble("handScore4Bonus");}
		    	if (rewards.has("handScoreDeltaPerPointReward")) {this.rewards.handScoreDeltaPerPointReward=rewards.getDouble("handScoreDeltaPerPointReward");}
		    	if (rewards.has("handScoreKeepalivePerPointReward")) {this.rewards.handScoreKeepalivePerPointReward=rewards.getDouble("handScoreKeepalivePerPointReward");}
		    	if (rewards.has("pointlessDrawPenalty")) {this.rewards.pointlessDrawPenalty=rewards.getDouble("pointlessDrawPenalty");}
		    	if (rewards.has("pointlessKeepPenalty")) {this.rewards.pointlessKeepPenalty=rewards.getDouble("pointlessKeepPenalty");}
		    	if (rewards.has("goodDiscardRecommendationReward")) {this.rewards.goodDiscardRecommendationReward=rewards.getDouble("goodDiscardRecommendationReward");}
		    	if (rewards.has("winReward")) {this.rewards.winReward=rewards.getDouble("winReward");}
		    	if (rewards.has("lossPenalty")) {this.rewards.lossPenalty=rewards.getDouble("lossPenalty");}
	    	}
	    	if (json.has("useUI")) {this.useUI = json.getBoolean("useUI");}
	    
	    this.setApproxHandsToPlay(this.approxHandsToPlay);
	    this.setGreedyAnnealHands(this.greedyAnnealHands);
    }
    
    public static void main(String[] args) {
    		try {
    			File f = File.createTempFile("GummyConfig", "json");
    			// File f = new File("xxxGummyConfig.json");
    			new GummyConfig().save(f.getPath());
    			System.out.println(GummyConfig.fromFile(f.getPath()).toString());
    			f.delete();
    		} catch(Exception e) {
    			System.out.println(e.getMessage());
    		}
    }
}
