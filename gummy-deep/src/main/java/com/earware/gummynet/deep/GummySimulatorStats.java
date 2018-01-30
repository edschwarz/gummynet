package com.earware.gummynet.deep;

import java.util.HashMap;
import java.util.Map;

public class GummySimulatorStats {
	public static int MOVING_AVERAGE_HANDS=100;
	public static int MOVING_AVERAGE_WINS=500;
	public static int MOVING_AVERAGE_DRAWS=500;
	public static int MOVING_AVERAGE_VOTES=3500;
	
	public int numHandsCompleted = 0; 
	public int numHandsWon = 0; 
	public int numHandsFlunked = 0;
	public int numHandsDrained = 0;
	public int numResets = 0;
	public double totReward = 0;
	public long totDuration = 0;
	public long totSteps = 0;
	public long avgHandSteps = 0;
	public long avgHandDuration = 0;
	public double avgHandReward = 0;
	public long maxHandSteps = Long.MIN_VALUE;
	public long maxHandDuration = 0;
	public double maxHandReward = -Double.MAX_VALUE;
	public long minHandSteps = Long.MAX_VALUE;
	public long minHandDuration = 0;
	public double minHandReward = Double.MAX_VALUE;
	public Map<String,Long> winMap = new HashMap<String,Long>();
	public Map<Integer,Long> drawMap = new HashMap<Integer,Long>();
	public Map<Integer,Long> discardMap = new HashMap<Integer,Long>();
	public Map<Integer,Long> discardVoteMap = new HashMap<Integer,Long>();
	public CircularBuffer movingAverageReward = new CircularBuffer(MOVING_AVERAGE_HANDS);
	public CircularBuffer movingAverageSteps = new CircularBuffer(MOVING_AVERAGE_HANDS);
	public CircularBuffer movingAverageMsec = new CircularBuffer(MOVING_AVERAGE_HANDS);
	public CircularBuffer movingAverageWins = new CircularBuffer(MOVING_AVERAGE_WINS);
	public CircularBuffer drawMA = new CircularBuffer(MOVING_AVERAGE_DRAWS);
	public CircularBuffer discardVoteMA = new CircularBuffer(MOVING_AVERAGE_VOTES);

	public long getNumHandsCompleted() {return numHandsCompleted;}
	
	public GummySimulatorStats() {
		drawMap.put(0,0L);
		drawMap.put(1,0L);
		discardVoteMap.put(0,0L);
		discardVoteMap.put(1,0L);
		for (int i=0; i<8; i++) {
			discardMap.put(i,0L);
		}
	}
	
	public void addReset() {
		numResets++;
	}
	
	public void addDraw(int action) {
		addAction(action, drawMap);
		drawMA.store(action);
	}
	
	public void addDiscard(int action) {
		addAction(action, discardMap);
	}
	
	public void addDiscardVote(int action) {
		addAction(action, discardVoteMap);
		discardVoteMA.store(action);
	}
	
	private void addAction(int action, Map<Integer,Long> map) {
		Long incumbent = map.get(action);
		if (incumbent==null) {
			incumbent=new Long(0L);
			map.put(action, incumbent);
		}
		map.put(action, incumbent.longValue()+1);
	}
	
	public void addWin(GummyState state) {
		numHandsWon++;
		movingAverageWins.store(1);
		String playerName = state.getGinHand().getCurrentPlayer().getName();
		Long incumbent = winMap.get(playerName);
		if (incumbent == null) {
			incumbent = new Long(0);
		}
		winMap.put(playerName, incumbent+1);
		addCompletedHand(state);
	}
	
	public void addFlunk(GummyState state) {
		numHandsFlunked++;
		movingAverageWins.store(0);
		addCompletedHand(state);
	}
	
	public void addDrain(GummyState state) {
		numHandsDrained++;
		movingAverageWins.store(0);
		addCompletedHand(state);
	}
	
	private void addCompletedHand(GummyState state) {
		numHandsCompleted++;
		
		if (state.endTime < state.startTime) {
			state.endTime = System.currentTimeMillis();
		}
		long duration =  state.endTime-state.startTime;
		this.totDuration += duration;
		this.movingAverageMsec.store((int)duration);
		this.avgHandDuration = this.totDuration/this.numHandsCompleted;
		
		this.totSteps += state.stepCount;
		this.movingAverageSteps.store(state.stepCount);
		this.avgHandSteps = this.totSteps / this.numHandsCompleted; 
		if (state.stepCount<this.minHandSteps) {
			this.minHandSteps = state.stepCount;
			this.minHandDuration = duration;
		} 
		if (state.stepCount>this.maxHandSteps) {
			this.maxHandSteps = state.stepCount;
			this.maxHandDuration = duration;
		}
		
		double reward = state.handReward;
		this.totReward += reward;
		this.movingAverageReward.store((int)reward);
		this.avgHandReward = this.totReward / this.numHandsCompleted; 
		if (reward<this.minHandReward) {
			this.minHandReward = reward;
		} 
		if (reward>this.maxHandReward) {
			this.maxHandReward = reward;
		} 
		
		processActivations(state);
	}

	public double getDrawRatio() {
		double deck = drawMap.get(1).doubleValue();
		double pile = drawMap.get(0).doubleValue();
		
		return (deck/(deck+pile));
	}
	
	public double getDiscardVoteRatio() {
		double deck = discardVoteMap.size()>1?discardVoteMap.get(1).doubleValue():0;
		double pile = discardVoteMap.size()>1?discardVoteMap.get(0).doubleValue():1;
		
		return (deck/(deck+pile));
	}
	
	public String toString() {
		return ""
		+ "                  numResets: " +  numResets + "\n"  
		+ "          numHandsCompleted: " +  numHandsCompleted + "\n"  
		+ "                numHandsWon: " +  numHandsWon + "\n" 
		+ "           winRatio(avg/ma): " +  String.format("%2.2f%%", getWinRatio()*100.0) + '/' + String.format("%2.2f%%", getWinRatioMA()*100.0) + "\n"
		+ "                     winMap: " +  winMap + "\n"
		+ "            numHandsFlunked: " +  numHandsFlunked + "\n"
		+ "            numHandsDrained: " +  numHandsDrained + "\n"
		+ "                   totSteps: " +  totSteps + "\n"
		+ "                totDuration: " +  hms(totDuration) + " (" + totDuration + ")" + "\n"
		+ " steps/hand(avg/min/max/ma): " +  avgHandSteps + '/' + minHandSteps + '/' + maxHandSteps + '/' + String.format("%01.2f", movingAverageSteps.average()) + "\n"
		+ " turns/hand(avg/min/max/ma): " +  avgHandSteps/10 + '/' + minHandSteps/10 + '/' + maxHandSteps/10 + '/' + String.format("%01.2f", movingAverageSteps.average()/10) + "\n"
		+ "reward/hand(avg/min/max/ma): " +  String.format("%01.2f", avgHandReward) + '/' + String.format("%01.2f", minHandReward) + '/' + String.format("%01.2f", maxHandReward) + '/' + String.format("%01.2f", movingAverageReward.average()) + "\n"
		+ "  msec/hand(avg/min/max/ma): " +  avgHandDuration + '/' + minHandDuration + '/' + maxHandDuration + '/' + String.format("%01.2f", movingAverageMsec.average()) + "\n"
		+ "activations(tot/net/avg/stddev): " +  String.format("%01.4f/%01.4f/%01.4f/%01.4f", totalActivations, netActivations, averageActivation, stddevActivation) + "\n"
		// + "      maxHandDurationNumber: " +  maxHandDurationNumber + "\n"
		// + "      minHandDurationNumber: " +  minHandDurationNumber + "\n"
		+ "                    drawMap: " +  "{PILE=" + drawMap.get(0) + ", DECK=" + drawMap.get(1) + "}  (" + String.format("%3.2f%%", getDrawRatio()*100) + ")" +  "  ma " + (((double)drawMA.total())/(double)(MOVING_AVERAGE_DRAWS))*100 + "%" + "\n"
		+ "                 discardMap: " +  discardMap + "\n"
		+ "             discardVoteMap: " +  "{KEEP=" + discardVoteMap.get(0) + ", DISCARD=" + discardVoteMap.get(1) + "}  (" + String.format("%3.2f%%", getDiscardVoteRatio()*100) + ")" + "  ma " + (((double)discardVoteMA.total())/(double)(MOVING_AVERAGE_VOTES))*100 + "%" + "\n"
		;
	}
	
	public double getWinRatio() {
		return (((double)numHandsWon)/numHandsCompleted);
	}
	
	public double getWinRatioMA() {
		return movingAverageWins.average();
	}
	
	private String hms(long millis) {
		long seconds = millis / 1000;
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60));
		return String.format("%d:%02d:%02d", h,m,s);
	}
	
	private double totalActivations=1;
	private double netActivations=0;
	private double averageActivation=0;
	private double stddevActivation=0;
	private void processActivations(GummyState state) {
		double myActivations = 0;
		int len = state.totEncodingCalls;
		if (len>state.netValues.length) {
			len = state.netValues.length;
		}
		for (int i=0; i<len; i++) {
			myActivations+=state.netValues[i];
		}
		double avg = myActivations/len;
		double se = 0;
		for (int i=0; i<len; i++)
		{
		    se += Math.pow(state.netValues[i]-avg,2) / len;
		}
		double standardDeviation = Math.sqrt(se);
		stddevActivation = standardDeviation;
		totalActivations += state.totEncodingCalls;
		netActivations += myActivations;
		
		averageActivation = netActivations/totalActivations;
	}
}
