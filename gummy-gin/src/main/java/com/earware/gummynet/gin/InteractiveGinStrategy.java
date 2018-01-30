package com.earware.gummynet.gin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;

public class InteractiveGinStrategy implements GinStrategy {
	private PrintStream out = System.out;
	private LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
	
	public InteractiveGinStrategy() {}
	
	public InteractiveGinStrategy(InputStream in, PrintStream out) {
		this.in = new LineNumberReader(new InputStreamReader(in));;
		this.out = out;
	}
	
	public Move.Draw.Source getDrawSource(GinHand hand) {
		Move.Draw.Source drawSource = null; 
		presentForDraw(hand);
		while (drawSource==null) {
			try {
				out.println("DECK or PILE?");
				String line = in.readLine();
				if (line.toUpperCase().startsWith("D")) {
					drawSource=Move.Draw.Source.DECK;
				} else if (line.toUpperCase().startsWith("P")) {
					drawSource=Move.Draw.Source.PILE;
				}
			} catch (IOException e) {
				out.println(e.getMessage()); 
			}
		}
		return drawSource!=null?drawSource:Move.Draw.Source.DECK;
	}
	
	private void presentForDraw(GinHand ginHand) {
		if (ginHand.moves.size()>1) {
			Move opponentMove = ginHand.moves.get(ginHand.moves.size()-2);
			Move.Draw.Source drawSource = opponentMove.draw.source;
			String drawCardStr = (drawSource==Move.Draw.Source.PILE) ?
					opponentMove.draw.card.toString() + " " : "";
			out.println("your opponent drew " + drawCardStr + "from the " + drawSource);
			out.println("your opponent discarded " + opponentMove.getDiscard().toString());
		} else {
			out.println("you are the first player");
		}
		out.println(ginHand.getCurrentPlayer().name + "'s hand: " 
				+ ginHand.getCurrentPlayerHand().toString() 
				+ "     PILE: " + ginHand.getDiscard());
	}
	
	public Card getDiscardCard(PlayerHand playerHand, GinHand hand) {
		int cardChoice = -1; 
		out.println(hand.getCurrentPlayer().name + "'s hand: " 
				+ hand.getCurrentPlayerHand().toString()); 
		while (cardChoice==-1) {
			try {
				out.println("Discard (1-8, left to right)?");
				String line = in.readLine();
				try {
					int candidate = Integer.parseInt(line);
					if (candidate>0 && candidate <9) {
						cardChoice = candidate;
					} else {
						out.println("cannot use \"" + line + "\" as a choice.");
					}
				} catch (NumberFormatException nfe) {
					out.println("cannot use \"" + line + "\" as a choice.");
				}
			} catch (IOException e) {
				out.println(e.getMessage()); 
			}
		}
		return playerHand.getCards().get(cardChoice-1);
	}
}
