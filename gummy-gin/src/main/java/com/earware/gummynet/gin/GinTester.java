package com.earware.gummynet.gin;

import java.util.*;
import java.io.*;

public class GinTester {
	static PrintStream out = System.out;
	public static class WinningHand {
		public List<Card> part1;
		public List<Card> part2;
		WinningHand(List<Card> a, List<Card> b) {part1=a; part2=b;}
		public String toString() {
			return '[' + Card.cardListToString(part1) + "] [" +  Card.cardListToString(part2) + ']'; 
		}
	}
	
	public static boolean wins(PlayerHand hand) {
		WinningHand winner = asWinningHand(hand);
		return winner != null; 
	}
	
	public static WinningHand asWinningHand(PlayerHand hand) {
		
		List<List<Card>> candidates = new ArrayList<List<Card>>();
		
		List<List<Card>> matches = findMatches(hand);
		candidates.addAll(matches);
		//if (matches.size()>0) {
		//	out.println("matches: ");
		//	for (List<Card> match : matches) {
		//		printCardList(match);
		//	}
		//}
		
		List<List<Card>> runs = findRuns(hand);
		candidates.addAll(runs);
		//if (runs.size()>0) {
		//	out.println("runs: ");
		//	for (List<Card> run : runs) {
		//		printCardList(run);
		//	}
		//}

		for (int i=0; i<candidates.size()-1; i++) {
			List<Card> a = candidates.get(i);
			for (int j=i+1; j<candidates.size(); j++) {
				List<Card> b = candidates.get(j);
				if (b.size()+a.size()==7) {
					boolean overlap=false;
					for (Card c : a) {
						if (b.contains(c)) {
							overlap=true;
							break;
						}
					}
					if (!overlap) {
						return new WinningHand(a,b);
					}
				}
			}
		}
		
		return null;
	}
	
	private static List<List<Card>> findRuns(PlayerHand hand) {
		return findRuns(hand,3,4);
	}
	public static List<List<Card>> findRuns(PlayerHand hand, int minLength, int maxLength) {
		List<List<Card>> runs = new ArrayList<List<Card>>();
		List<Card> run;
		
		List<Integer> cardints = new ArrayList<Integer>();
		for (Card card : hand.getCards()) {
			int cardint = cardToInt(card);
			cardints.add(cardint);
			if (card.rank.equals(Card.Rank.ACE)) {
				cardints.add(cardint+13);
			}
		}
		
		Collections.sort(cardints);

		for (int i=0; i<cardints.size()-1; i++) {
			int a = cardints.get(i);
			run = new ArrayList<Card>();
			run.add(intToCard(a));
			for (int j=i+1; j<cardints.size(); j++) {
				int b = cardints.get(j);
				if (b==a+1) {
					run.add(intToCard(b));
					a=b;
					if (run.size()>=minLength && run.size()<=maxLength) {
						runs.add(run);
						run = new ArrayList<Card>(run);
					}
				} else {
					break;
				}
			}
		}
		
		return runs;
	}
	
	@SuppressWarnings("incomplete-switch")
	private static int cardToInt(Card card) {
		int cardint = card.rankInt();
		switch (card.suit) {
			case DIAMONDS: cardint += 100; break;
			case SPADES: cardint += 200; break;
			case HEARTS: cardint += 300; break;
		}
		return cardint;
	}
	
	private static Card intToCard(int i) {
		int rankint = i%100;
		Card.Suit suit = Card.Suit.CLUBS;
		if (i>=300) {
			suit = Card.Suit.HEARTS;
		} else if (i>=200) {
			suit = Card.Suit.SPADES;
		} else if (i>=100) {
			suit = Card.Suit.DIAMONDS;
		}
		
		return new Card(suit, Card.rankFromInt(rankint));
	}
	
	private static List<List<Card>> findMatches(PlayerHand hand) {
		return findMatches(hand,3,4);
	}
	public static List<List<Card>> findMatches(PlayerHand hand, int minLength, int maxLength) {
		List<List<Card>> matches = new ArrayList<List<Card>>();
		List<Card> match;
		
		for (int i=0; i<hand.getCards().size()-1; i++) {
			Card a = hand.getCards().get(i);
			match = new ArrayList<Card>();
			match.add(a);
			for (int j=i+1; j<hand.getCards().size(); j++) {
				Card b = hand.getCards().get(j);
				if (a.rank.equals(b.rank)) {
					match.add(b);
				}
			}
			if (match.size()>=minLength && match.size()<=maxLength) {
				matches.add(match);
			}
		}
		
		return matches;
	}
	
	//private static void printCardList(List<Card> cardlist) {
	//	out.println(Card.cardListToString(cardlist));
	//}
	
}
