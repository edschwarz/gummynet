package com.earware.gummynet.deep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;

import com.earware.gummynet.deep.GummyMDP;
import com.earware.gummynet.deep.GummyState;
import com.earware.gummynet.gin.Card;
import com.earware.gummynet.gin.GinHand;
import com.earware.gummynet.gin.GinStrategy;
import com.earware.gummynet.gin.Move;
import com.earware.gummynet.gin.Player;
import com.earware.gummynet.gin.PlayerHand;
import com.earware.gummynet.gin.Move.Draw;
import com.earware.gummynet.gin.PlayGin;


public class StatsGummyMDP extends PlayGin {
	public static void main(String[] args) {
		try {
			new StatsGummyMDP().testStats();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	

	static GummyConfig gummyConfig = new GummyConfig();
	
	@Test
	public void testStats() throws Exception {
		gummyConfig = GummyConfig.fromFile("../zzzGummyConfig.json");
		int count = 20000;
		int wins = 0;
		Map<String,Integer> winMap = new HashMap<String, Integer>();
		long startTime = System.currentTimeMillis();
		
		// yuck. 
		RewardsRandomGinStrategy strategy = new StatsGummyMDP.SmartRewardsGinStrategy();
		// RandomInputsGinStrategy istrategy = new StatsGummyMDP.RandomInputsGinStrategy();
		RewardsRandomGinStrategy istrategy = new StatsGummyMDP.SmartRewardsGinStrategy();
		
		for (int i=0; i<count; i++) {
			
			GinHand ginHand = createGinHand(istrategy, strategy);
			GummyState gummyState = new GummyState(ginHand);
			gummyState.setInputWeights(gummyConfig.inputWeights);
			// istrategy.gummyState=gummyState;

			ginHand.deal();
			ginHand = playHand(ginHand);
			boolean didWin = ginHand.getCurrentPlayerHand().wins();
			if (didWin) {
				wins++;
				String winnerName = ginHand.getCurrentPlayer().getName();
				winMap.put(winnerName, winMap.get(winnerName)==null?1:winMap.get(winnerName)+1);
			}
		}
		long duration = System.currentTimeMillis() - startTime;
		LOGGER.info("won " + wins + " hands out of " + count + " in " + duration + "ms = " + String.format("%3.2f%%", ((double)wins/count)*100));
		LOGGER.info("winners: " + winMap);
		LOGGER.info("STATS:");
		LOGGER.info(strategy.toString());
		LOGGER.info("------------\n" + istrategy.toString());
	}

	////////////////////
	////////////////////
	////////////////////
	////////////////////
	public static class RandomInputsGinStrategy extends RandomGinStrategy {
		GummyState gummyState;
		double grandTotal = 0;
		int totalDrawInvocations=0;
		int totalDiscardInvocations=0;
		
		@Override
		public Move.Draw.Source getDrawSource(GinHand hand) {
			gummyState.toArray();
			double total = 1; // gummyState.netValues[gummyState.totEncodingCalls-1];
			grandTotal+=total;
			totalDrawInvocations++;
			// LOGGER.info("input total is: "+ total);
			if (totalDrawInvocations%100==0) {
				LOGGER.info("totalDrawInvocations: "+totalDrawInvocations + "     avg: " + getAverageInputTotal());
			}
			return super.getDrawSource(hand);
		}
		
		@Override
		public Card getDiscardCard(PlayerHand playerHand, GinHand hand) {
			gummyState.toArray();
			double total = 1; // gummyState.netValues[gummyState.totEncodingCalls-1];
			grandTotal+=total;
			totalDiscardInvocations++;
			// LOGGER.info("input total is: "+ total);
			if (totalDiscardInvocations%100==0) {
				LOGGER.info("totalDiscardInvocations: "+totalDiscardInvocations + "     avg: " + getAverageInputTotal());
			}
			return super.getDiscardCard(playerHand,hand);
		}
		
		public double getAverageInputTotal() {return grandTotal/totalDrawInvocations;}
		
	}
	
	////////////////////
	////////////////////
	////////////////////
	////////////////////
	public static class RewardsRandomGinStrategy extends RandomGinStrategy {
		
		int totPileDraws=0;
		int totDeckDraws=0;
		int totDrawCalls=0;
		int drawImprovements=0;
		int totDrawImprovementScore=0;
		int drawDegradations=0;
		int totDrawDegradationScore=0;

		int totDiscardCalls=0;
		int discardImprovements=0;
		int totDiscardImprovementScore=0;
		int discardDegradations=0;
		int totDiscardDegradationScore=0;
		
		Map<String,Integer> playCounterMap = new HashMap<String,Integer>();
		
		@Override
		public String toString() {
			return ""
			+ "  " + "totDrawCalls="+totDrawCalls
			+ "  " + "totPileDraws="+totPileDraws+" (" + String.format("%3.2f",((double)totPileDraws)/totDrawCalls*100) + "%)"
			+ "  " + "totDeckDraws="+totDeckDraws+" (" + String.format("%3.2f",((double)totDeckDraws)/totDrawCalls*100) + "%)"
			+ "  " + "drawImprovements="+drawImprovements+" (" + String.format("%3.2f",((double)drawImprovements)/totDrawCalls*100) + "%)"
			+ "  " + "averageImprovement="+ String.format("%3.2f",((double)totDrawImprovementScore)/totDrawCalls) 
			+ "  " + "drawDegradations="+drawDegradations+" (" + String.format("%3.2f",((double)drawDegradations)/totDrawCalls*100) + "%)"
			+ "  " + "averageDrawDegradation="+ String.format("%3.2f",((double)totDrawDegradationScore)/totDrawCalls)

			+ "  " + "totDiscardCalls="+totDiscardCalls
			+ "  " + "discardImprovements="+discardImprovements+" (" + String.format("%3.2f",((double)discardImprovements)/totDiscardCalls*100) + "%)"
			+ "  " + "averageImprovement="+ String.format("%3.2f",((double)totDiscardImprovementScore)/totDiscardCalls) 
			+ "  " + "discardDegradations="+discardDegradations+" (" + String.format("%3.2f",((double)discardDegradations)/totDiscardCalls*100) + "%)"
			+ "  " + "averageDiscardDegradation="+ String.format("%3.2f",((double)totDiscardDegradationScore)/totDiscardCalls)
			
			+ "  " + "playCounterMap="+ playCounterMap 
			;
		}
		
		protected Move.Draw.Source _getDrawSource(GinHand hand) {
			return super.getDrawSource(hand);
		}
		
		protected Card _getDiscardCard(PlayerHand playerHand, GinHand hand) {
			return super.getDiscardCard(playerHand, hand);
		}
		
		protected double score(GinHand hand) {
			return score(hand, gummyConfig.rewards);
		}
		private double score(GinHand hand, GummyMDP.Rewards rwds) {
			return GummyMDP.score(hand.getCurrentPlayerHand(), rwds);
		}
		private void addPlay(GinHand hand) {
			Integer playCounter = playCounterMap.get(hand.getCurrentPlayer().getName());
			if (playCounter==null) {
				playCounter = 0;
			}
			playCounterMap.put(hand.getCurrentPlayer().getName(), ++playCounter);
		}

		double drawScore = 7777;
		double discardScore = 7777;
		Draw.Source drawSource = Draw.Source.DECK;
		
		@Override
		public Move.Draw.Source getDrawSource(GinHand hand) {
			addPlay(hand);
			
			discardScore = score(hand); // because we're about to draw
			if (discardScore > drawScore) {  
				discardImprovements++;
				totDiscardImprovementScore+=discardScore-drawScore;
			} else if (discardScore < drawScore) {
				discardDegradations++;
				totDiscardDegradationScore+=discardScore-drawScore;
			}
			totDrawCalls++;
			drawSource = _getDrawSource(hand);
			return drawSource;
		}
		
		@Override
		public Card getDiscardCard(PlayerHand playerHand, GinHand hand) {
			drawScore = score(hand); // because we're about to discard
			if (drawSource == Draw.Source.PILE) {
				totPileDraws++;
				// see if it helped
				if (drawScore > discardScore) {
					drawImprovements++;
					totDrawImprovementScore+=drawScore-discardScore;
				} else if (drawScore < discardScore) {
					drawDegradations++;
					totDrawDegradationScore+=drawScore-discardScore;
				}
			} else {
				totDeckDraws++;
			}
			totDiscardCalls++;
			Card result = _getDiscardCard(playerHand, hand);
			return result;
		}
	}
	
	//////////////
	public static class SmartRewardsGinStrategy extends RewardsRandomGinStrategy {
		@Override
		protected Move.Draw.Source _getDrawSource(GinHand hand) {
			PlayerHand lookAheadHand = hand.getCurrentPlayerHand().copy();
			double preDrawScore = GummyMDP.score(lookAheadHand, gummyConfig.rewards);
			lookAheadHand.getCards().add(hand.getDiscard());
			double postDrawScore = GummyMDP.score(lookAheadHand, gummyConfig.rewards);
			if (postDrawScore>preDrawScore) {
				return Move.Draw.Source.PILE;
			} else {
				return Move.Draw.Source.DECK;
			}
		}
		
		@Override
		protected Card _getDiscardCard(PlayerHand playerHand, GinHand hand) {
			PlayerHand currentHand = hand.getCurrentPlayerHand().copy();
			double preDiscardScore = GummyMDP.score(currentHand, gummyConfig.rewards);
			List<Card> candidates = new ArrayList<Card>();
			for (int i=0; i<currentHand.getCards().size(); i++) {
				PlayerHand lookAheadHand = hand.getCurrentPlayerHand().copy();
				lookAheadHand.getCards().remove(i);
				double postDiscardScore = GummyMDP.score(lookAheadHand, gummyConfig.rewards);
				if (postDiscardScore>=preDiscardScore) {
					// it didn't hurt - it's a candidate
					candidates.add(currentHand.getCards().get(i));
				}
			}
			if (candidates.size()>0) {
				Collections.shuffle(candidates);
				return candidates.get(0);
			} else {
				return currentHand.getCards().get((int)(Math.random()*currentHand.getCards().size()));
			}
		}		
	}
	
	//////////////
	public static GinHand createGinHand(GinStrategy strategyOne, GinStrategy strategyTwo) {
		Player p1 = new Player("joe");
		Player p2 = new Player("annie");
		GinHand hand = new GinHand(p1, strategyOne, p2, strategyTwo);
		hand.deal();
		return hand;
	}

    public static Logger LOGGER = Logger.getLogger(GummyMDP.class.getPackage().getName()); 
}
