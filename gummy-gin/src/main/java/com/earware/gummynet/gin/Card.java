package com.earware.gummynet.gin;

import java.util.List;

public class Card implements Comparable<Card> {
	enum Suit {SPADES, HEARTS, DIAMONDS, CLUBS};
	enum Rank {ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING};
	
	final Suit suit;
	final Rank rank; 
	public Suit getSuit() {return suit;}
	public Rank getRank() {return rank;}

	public Card(Suit suit, Rank rank) {
		this.suit=suit;
		this.rank=rank;
	}
	
	@Override
	public String toString() {
		return rankString(rank) + suitChar(suit);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Card) {
			Card c = (Card) o;  
			return this.rank.equals(c.rank) && this.suit.equals(c.suit);
		}
		return false; 
	}
			
	public int compareTo(Card o) {
		return new Integer(Deck.card2Int(this)).compareTo(Deck.card2Int(o));
	}
	
	public int rankInt() {
		return Card.rankInt(this.rank);
	}
	
	////// static 
	
	public static char suitChar(Suit suit) {
		switch (suit) {
		case SPADES: return 'S';
		case HEARTS: return 'H';
		case DIAMONDS: return 'D';
		case CLUBS: return 'C';
		}
		throw new IllegalArgumentException("no good suit:" + suit);
	}
	
	public static Suit suitFromChar(char c) {
		switch (c) {
		case 'S' : return Suit.SPADES;
		case 'H' : return Suit.HEARTS;
		case 'D' : return Suit.DIAMONDS;
		case 'C' : return Suit.CLUBS;
		}
		throw new IllegalArgumentException("no good suit char:" + c);
	}
	
	public static Card fromString(String s) {
		try {
			Suit suit = suitFromChar(s.charAt(s.length()-1));
			Rank rank = rankFromString(s);
			return new Card(suit,rank);
		} catch (Exception e) {
			throw new IllegalArgumentException("no good card string:" + s);
		}
	}
	
	public static int rankInt(Rank r) {
		switch (r) {
		case ACE: return 1;
		case TWO: return 2;
		case THREE: return 3;
		case FOUR: return 4;
		case FIVE: return 5;
		case SIX: return 6;
		case SEVEN: return 7;
		case EIGHT: return 8;
		case NINE: return 9;
		case TEN: return 10;
		case JACK: return 11;
		case QUEEN: return 12;
		case KING: return 13;
		}
		throw new IllegalArgumentException("no good rank:" + r);
	}

	public static Rank rankFromInt(int i) {
		switch (i) {
		case 1: return Rank.ACE;
		case 2: return Rank.TWO;
		case 3: return Rank.THREE;
		case 4: return Rank.FOUR;
		case 5: return Rank.FIVE;
		case 6: return Rank.SIX;
		case 7: return Rank.SEVEN;
		case 8: return Rank.EIGHT;
		case 9: return Rank.NINE;
		case 10: return Rank.TEN;
		case 11: return Rank.JACK;
		case 12: return Rank.QUEEN;
		case 13: return Rank.KING;
		case 14: return Rank.ACE;
		}
		throw new IllegalArgumentException("no good int:" + i);
	}
	
	@SuppressWarnings("incomplete-switch")
	public static String rankString(Rank r) {
		switch (r) {
		case ACE: return "A";
		case JACK: return "J";
		case QUEEN: return "Q";
		case KING: return "K";
		}
		return Integer.toString(rankInt(r));
	}
	
	public static Rank rankFromString(String s) {
		try {
			char c = s.charAt(0);
			switch (c) {
				case 'A': return Rank.ACE; 
				case 'K': return Rank.KING; 
				case 'Q': return Rank.QUEEN; 
				case 'J': return Rank.JACK; 
				case '2': return Rank.TWO; 
				case '3': return Rank.THREE; 
				case '4': return Rank.FOUR; 
				case '5': return Rank.FIVE; 
				case '6': return Rank.SIX; 
				case '7': return Rank.SEVEN; 
				case '8': return Rank.EIGHT; 
				case '9': return Rank.NINE; 
				case '1': return Rank.TEN;
			}
		} catch (Exception e) {
		}
		throw new IllegalArgumentException("no good rankString:" + s);
	}
	
	public static String cardListToString(List<Card> cardlist) {
		StringBuffer  sb = new StringBuffer();
		for (Card card : cardlist) {
			sb.append(card.toString());
			sb.append(' ');
		}
		return sb.toString(); 
	}

}
