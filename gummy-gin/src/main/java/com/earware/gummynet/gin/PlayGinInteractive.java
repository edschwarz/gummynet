package com.earware.gummynet.gin;

import java.io.*;

public class PlayGinInteractive {
	static PrintStream out = System.out;
	static InputStream in = System.in;
	
	public static void main(String[] args) {
		playHand(); 
	}
	
	public static void playHand() {
		Player p1 = new Player("joe");
		Player p2 = new Player("annie");
		GinHand hand = new GinHand(p1,new InteractiveGinStrategy(), p2, new PlayGin.DumbassGinStrategy());
		hand.deal();
		GinHand.Playing winner = hand.play();
		if (winner != null) {
			out.println("WINNER: " + winner.getPlayer().name + "  " + GinTester.asWinningHand(winner.getPlayerHand()).toString());
		} else {
			out.println("NO WINNER");
		}
	}
}
