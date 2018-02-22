package com.earware.gummynet.deep;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.earware.gummynet.gin.Card;
import com.earware.gummynet.gin.Deck;
import com.earware.gummynet.gin.GinHand;
import com.earware.gummynet.gin.GinStrategy;
import com.earware.gummynet.gin.PlayGin;

public class GummyDeepPlayGin {
	public static void main(String[] args) {
		int count = 40;
		String model1 = args[0];
		String model2 = model1;
		if (args.length>1) {
			model2 = args[1];
		}
		if (args.length>2) {
			count = Integer.parseInt(args[2]);
		}
		
		play(model1, model2, count);
	}
	
	public static class Stats {
		public int wins = 0;
		public int hands = 0;
		public long duration = 0L;
		public int drawPileCount = 0;
		public int drawDeckCount = 0;
		public int voteKeepCount = 0;
		public int voteDiscardCount = 0;
		public Map<String,Integer> winMap = new HashMap<String,Integer>();
		public int[] winCounts = {0,0};
		public double[] p1WinHistogram = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		public double[] p2WinHistogram = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		public int turns() {return drawPileCount+drawDeckCount;} 
		public int totalDecisions() {return turns() + voteKeepCount+voteDiscardCount;} 
		public String toString() {
			return    " winners: " + winMap + " (p1=" + winCounts[0] + ", p2=" + winCounts[1] + ")" + String.format(" (p1=%3.2f%%, p2=%3.2f%%)", (((double)winCounts[0]/hands)*100), (((double)winCounts[1]/hands)*100))
					+ " total " + wins + "/" + hands + String.format(" (%3.2f%%)", ((double)wins/hands)*100)
					+ " fitness: [p1=" + String.format("%5.3f", p1Fitness()) + ", p2=" + String.format("%5.3f", p2Fitness()) + "]"  
					+ " time: " + duration + "ms "   + String.format("(%3.2f ms/hand)", ((double)duration/hands))
					+ " decision counts: PILE=" + drawPileCount 
			        					+ " DECK=" + drawDeckCount 
			        					+ " KEEP=" + voteKeepCount 
			        					+ " DISCARD=" + voteDiscardCount
			  ;
		}
		
		public double p1Fitness() {
			return GummySmartGinStrategy.evaluateHistogramFit(this.p1WinHistogram);
		}
		public double p2Fitness() {
			return GummySmartGinStrategy.evaluateHistogramFit(this.p2WinHistogram);
		}
	}
	
	public static Stats play(String model1, String model2, int count) {
		return play(model1, null, model2, null, count);
	}
	
	public static Stats play(String model1, GummyMDP.Rewards rewards1, String model2, GummyMDP.Rewards rewards2, int count) {
		LOGGER.info("**** **** GummyDeepPlayGin playing " + count + " hands using models: " + model1 + " " + model2);
		Stats stats = new Stats();
		long startTime = System.currentTimeMillis();
		for (int i=0; i<count; i++) {
			GinStrategy strategy1 = getGinStrategy(model1, rewards1);
			GinStrategy strategy2 = getGinStrategy(model2, rewards2);
			GinHand ginHand = PlayGin.playHand(strategy1,strategy2,200);
			stats.hands++;
			boolean didWin = ginHand.getCurrentPlayerHand().wins();
			stats.duration = System.currentTimeMillis() - startTime;
			if (didWin) {
				reportWin(stats, ginHand);
			} 
			
			if (strategy1 instanceof GummyDeepGinStrategy) {
				stats.drawPileCount += ((GummyDeepGinStrategy)strategy1).stats.drawMap.get(0);
				stats.drawDeckCount += ((GummyDeepGinStrategy)strategy1).stats.drawMap.get(1);
				stats.voteKeepCount += ((GummyDeepGinStrategy)strategy1).stats.discardVoteMap.get(0);
				stats.voteDiscardCount += ((GummyDeepGinStrategy)strategy1).stats.discardVoteMap.get(1);
			}
			if (strategy2 instanceof GummyDeepGinStrategy) {
				stats.drawPileCount += ((GummyDeepGinStrategy)strategy2).stats.drawMap.get(0);
				stats.drawDeckCount += ((GummyDeepGinStrategy)strategy2).stats.drawMap.get(1);
				stats.voteKeepCount += ((GummyDeepGinStrategy)strategy2).stats.discardVoteMap.get(0);
				stats.voteDiscardCount += ((GummyDeepGinStrategy)strategy2).stats.discardVoteMap.get(1);
			}
			if ((i+1)%10==0) {
				LOGGER.info("**** " + (i+1) + " hands completed");
				LOGGER.info(report(stats) // + "  ratios: (1) " + strategy1.decisionMap + "   (2) " + strategy2.decisionMap
						// + "    stats(1): " + strategy1.stats.toString() + "    stats(2): "+ strategy2.stats.toString()
						+ " overall counts: PILE=" + stats.drawPileCount + " DECK=" + stats.drawDeckCount + " KEEP=" + stats.voteKeepCount + "  DISCARD=" + stats.voteDiscardCount);
			}
		}
		LOGGER.info("**** FINAL REPORT:");
		LOGGER.info(report(stats));
		LOGGER.info(" overall counts: PILE=" + stats.drawPileCount + " DECK=" + stats.drawDeckCount + " KEEP=" + stats.voteKeepCount + "  DISCARD=" + stats.voteDiscardCount);
		return stats;
	}
	
	private static void reportWin(Stats stats, GinHand ginHand) {
		stats.wins++;
		String winnerName = ginHand.getCurrentPlayer().getName();
		Integer currentWins = stats.winMap.get(winnerName);
		if (currentWins==null) {
			currentWins=0;
		}
		stats.winMap.put(winnerName,  currentWins+1);
		if (winnerName.equals(ginHand.getPlayerOne().name)) {
			stats.winCounts[0]++;
			for (Card c : ginHand.getCurrentPlayerHand().getCards()) {
				stats.p1WinHistogram[Deck.card2Int(c)]++;
			}
		} else {
			stats.winCounts[1]++;
			for (Card c : ginHand.getCurrentPlayerHand().getCards()) {
				stats.p2WinHistogram[Deck.card2Int(c)]++;
			}
		}
	}
	
	protected static GinStrategy getGinStrategy(String s, GummyMDP.Rewards rewards) {
		if (s==null || s.length()==0 | s.equals("bot")) {
			return new GummySmartGinStrategy(rewards!=null?rewards:new GummyMDP.Rewards());
		} else if (s.equals("random")) {
			return new PlayGin.RandomGinStrategy();
		} else {
			return new GummyDeepGinStrategy(s);
		}
	}
	
	public static String report(Stats stats) {
		return stats.toString();
	}

    public static Logger LOGGER = Logger.getLogger(GummyDeepGinStrategy.class.getName()); 
}
