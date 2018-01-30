package com.earware.gummynet.deep;
import java.io.*;
import java.util.*;

import com.earware.gummynet.gin.*;

public class PlayGin2 {
	static PrintStream out = System.out;
	public static Map<String,Integer> winMap = new HashMap<String,Integer>();
	
	// Dumbass 0.78% win
	// Random 1.48% win
	
	public static void main(String[] args) {
		int count = 100000;
		int wins = 0;
		long startTime = System.currentTimeMillis();
		for (int i=0; i<count; i++) {
			boolean didWin = playHand(new RandomGinStrategy(), new GummySmartGinStrategy(new GummyMDP.Rewards()));
			if (didWin) {
				wins++;
			}
		}
		long duration = System.currentTimeMillis() - startTime;
		out.println("won " + wins + " hands out of " + count + " in " + duration + "ms = " + String.format("%3.2f%%", ((double)wins/count)*100));
		out.println("winners: " + winMap);
	}
	
	public static boolean playHand(GinStrategy strategyOne, GinStrategy strategyTwo) {
		return playHand(strategyOne, strategyTwo, Integer.MAX_VALUE);
	}
	
	public static boolean playHand(GinStrategy strategyOne, GinStrategy strategyTwo, int maxTurns) {
		Player p1 = new Player("joe");
		Player p2 = new Player("annie");
		GinHand hand = new GinHand(p1, strategyOne, p2, strategyTwo);
		return playHand(hand, maxTurns);
	}
	
	public static boolean playHand(GinHand hand) {
		return playHand(hand, Integer.MAX_VALUE);
	}
	public static boolean playHand(GinHand hand, int maxTurns) {
		boolean won = false;
		hand.deal();
		GinHand.Playing winner = hand.play(maxTurns);
		if (winner != null) {
			out.println("WINNER: " + winner.getPlayer().name + "  " + GinTester.asWinningHand(winner.getPlayerHand()).toString());
			Integer winCount = winMap.get(winner.getPlayer().name);
			if (winCount == null) {
				winCount = new Integer(0);
			}
			winMap.put(winner.getPlayer().name, winCount+1);
			won = true; 
		}
		return won;
	}
	
	public static class DumbassGinStrategy implements GinStrategy {
		public Move.Draw.Source getDrawSource(GinHand hand) {
			return Move.Draw.Source.DECK;
		}
		
		public Card getDiscardCard(PlayerHand playerHand, GinHand hand) {
			return playerHand.getCards().get(3);
		}
	}
	
	public static class RandomGinStrategy implements GinStrategy {
		public Move.Draw.Source getDrawSource(GinHand hand) {
			return Math.random()<0.5? Move.Draw.Source.DECK : Move.Draw.Source.PILE;
		}
		
		public Card getDiscardCard(PlayerHand playerHand, GinHand hand) {
			return playerHand.getCards().get((int)(Math.random()*8));
		}
	}
	
	
}
