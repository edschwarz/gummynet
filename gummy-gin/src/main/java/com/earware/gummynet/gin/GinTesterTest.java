package com.earware.gummynet.gin;

import java.util.*;
import java.io.*;

public class GinTesterTest {
	static PrintStream out = System.out;
	
	public static void main(String[] args) {
		out.println("BASIC");
		testBasic();
		
		int deals = 100000;
		out.println("DEAL " + deals);
		testDeal(deals);
		
		deals=10000000;
		out.println("DEALT WINNER " + deals);
		testRandomWin(deals);		
	}
	
	private static void testDeal(int tries) {
		Deck deck = new Deck();
		List<Card> cards;

		for (int ii=0; ii<tries; ii++) {
			deck.shuffle();
			cards = new ArrayList<Card>();
			for (int i=0; i<7; i++) {
				cards.add(deck.next());
			}
			PlayerHand hand = new PlayerHand(cards);
			if (testHand(hand)) {
				out.println("WINNER (" + ii + "): " + GinTester.asWinningHand(hand).toString());
			}
		}
	}
	
	private static void testRandomWin(int tries) {
		Deck deck = new Deck();
		List<Card> cards;
		int totalWins = 0;

		for (int ii=0; ii<tries; ii++) {
			deck.shuffle();
			cards = new ArrayList<Card>();
			for (int i=0; i<7; i++) {
				cards.add(deck.next());
			}
			PlayerHand hand = new PlayerHand(cards);
			if (hand.wins()) {
				out.println("WINNER (" + ii + "): " + GinTester.asWinningHand(hand).toString());
				totalWins++;
			}
		}
		out.println("total wins: " + totalWins + " out of " + tries);

	}	
	
	private static void testBasic() {
		List<Card> cards;
		
		cards = new ArrayList<Card>();

		// two matches
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.TEN));
		testHand(new PlayerHand(cards), true);

		// match and run
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.JACK));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.QUEEN));
		testHand(new PlayerHand(cards), true);

		// two runs
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.SEVEN));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.EIGHT));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.FIVE));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.SIX));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.QUEEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.JACK));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.FIVE));
		testHand(new PlayerHand(cards), true);

		// loser
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TWO));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.JACK));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.QUEEN));
		testHand(new PlayerHand(cards), false);

		// 7-card run
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.SIX));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.SEVEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.EIGHT));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.NINE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.JACK));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.QUEEN));
		testHand(new PlayerHand(cards), true);

		// run an match
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.NINE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.JACK));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.QUEEN));
		testHand(new PlayerHand(cards), true);

		// 6-card run
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.SIX));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.SEVEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.EIGHT));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.NINE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.JACK));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.QUEEN));
		testHand(new PlayerHand(cards), false);

		// 3 pairs 
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.SIX));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.SIX));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.EIGHT));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.EIGHT));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.QUEEN));
		testHand(new PlayerHand(cards), false);

		// 4 of a kind plus 2 of a kind 
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.TWO));
		testHand(new PlayerHand(cards), false);

		// 4 of a kind plus 2-run plus pair 
		cards.clear();
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.SPADES,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.ACE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.JACK));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.JACK));
		testHand(new PlayerHand(cards), false);

		// 4-run plus 2-run plus pair 
		cards.clear();
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.SIX));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.SEVEN));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.EIGHT));
		cards.add(new Card(Card.Suit.DIAMONDS,Card.Rank.NINE));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.TEN));
		cards.add(new Card(Card.Suit.CLUBS,Card.Rank.JACK));
		cards.add(new Card(Card.Suit.HEARTS,Card.Rank.JACK));
		testHand(new PlayerHand(cards), false);

	}
	
	private static void testHand(PlayerHand hand, boolean expected) {
		boolean wins = testHand(hand);
		out.println("HAND: " + hand.toString());
		out.println("Wins: " + wins);
		if (!wins==expected) {
			out.println("!!!!!!!!!!!!!!!!!!!!! WRONG ^^^^^^^^ !!!!!!!!!!!!!!!!!!!");
		}
	}
	private static boolean testHand(PlayerHand hand) {
		return hand.wins();
	}

}
