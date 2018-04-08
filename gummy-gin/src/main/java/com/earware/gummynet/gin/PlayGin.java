package com.earware.gummynet.gin;

import java.io.*;
import java.util.*;

public class PlayGin {
	public static PrintStream log = System.out;
	// public static Map<String,Integer> winMap = new HashMap<String,Integer>();
	
	// Dumbass 0.78% win
	// Random 1.48% win
	
	public static void main(String[] args) {
		int count = 1000000;
		int wins = 0;
		long startTime = System.currentTimeMillis();
		Map<String,Integer> winMap = new HashMap<String,Integer>();
		for (int i=0; i<count; i++) {
			GinHand hand = playHand(new RandomGinStrategy(), new RandomGinStrategy());
			if (hand.getCurrentPlayerHand().wins()) {
				wins++;
			}
		}
		long duration = System.currentTimeMillis() - startTime;
		log.println("won " + wins + " hands out of " + count + " in " + duration + "ms = " + String.format("%3.2f%%", ((double)wins/count)*100));
		log.println("winners: " + winMap);
	}
	
	public static GinHand playHand(GinStrategy strategyOne, GinStrategy strategyTwo) {
		return playHand(strategyOne, strategyTwo, Integer.MAX_VALUE);
	}
	
	public static GinHand playHand(GinStrategy strategyOne, GinStrategy strategyTwo, int maxTurns) {
		return playHand(strategyOne, strategyTwo, maxTurns, true);
	}
	
	public static GinHand playHand(GinStrategy strategyOne, GinStrategy strategyTwo, int maxTurns, boolean logWins) {
		Player p1 = new Player("joe");
		Player p2 = new Player("annie");
		GinHand hand = new GinHand(p1, strategyOne, p2, strategyTwo);
		return playHand(hand, maxTurns, logWins);
	}
	
	public static GinHand playHand(GinHand hand) {
		return playHand(hand, Integer.MAX_VALUE);
	}
	
	public static GinHand playHand(GinHand hand, int maxTurns) {
		return playHand(hand, maxTurns, true);
	}
	
	public static GinHand playHand(GinHand hand, int maxTurns, boolean logWins) {
		hand.deal();
		GinHand.Playing winner = hand.play(maxTurns);
		if (winner != null) {
			log.println("WINNER: " + winner.getPlayer().name + "  " + GinTester.asWinningHand(winner.getPlayerHand()).toString());
		}
		return hand;
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
