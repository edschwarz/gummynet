package com.earware.gummynet.gin;

import java.util.*;

public class Deck {
	private Stack<Card> undealt;
	
	public Deck() {
		undealt = new Stack<Card>();
		shuffle();
	}
	
	public Card next() {
		if (getUndealt().size()>0) { 
			return getUndealt().pop();
		} else {
			return null; 
		}
	}
	
	public boolean isEmpty() {
		return getUndealt().size()<1;
	}
	
	public void shuffle() {
		List<Card> cards = new ArrayList<Card>(_cardList);
		Collections.shuffle(cards);
		getUndealt().clear();
		for (Card card : cards) {
			getUndealt().push(card);
		}
	}
	
	public boolean contains(Card card) {
		return this.getUndealt().contains(card);
	}
	
	@SuppressWarnings("unchecked")
	public Deck copy() {
		Deck deck = new Deck();
		deck.undealt = (Stack<Card>)getUndealt().clone();
		return deck;
	}
	
	public static int card2Int(Card card) {
		return _cardList.indexOf(card);
	}
	
	public static Card int2Card(int i) {
		return _cardList.get(i);
	}
	
	public Stack<Card> getUndealt() {
		return undealt;
	}

	private static List<Card> _cardList = new ArrayList<Card>(Arrays.asList(
		new Card(Card.Suit.CLUBS,Card.Rank.ACE),
		new Card(Card.Suit.CLUBS,Card.Rank.TWO),
		new Card(Card.Suit.CLUBS,Card.Rank.THREE),
		new Card(Card.Suit.CLUBS,Card.Rank.FOUR),
		new Card(Card.Suit.CLUBS,Card.Rank.FIVE),
		new Card(Card.Suit.CLUBS,Card.Rank.SIX),
		new Card(Card.Suit.CLUBS,Card.Rank.SEVEN),
		new Card(Card.Suit.CLUBS,Card.Rank.EIGHT),
		new Card(Card.Suit.CLUBS,Card.Rank.NINE),
		new Card(Card.Suit.CLUBS,Card.Rank.TEN),
		new Card(Card.Suit.CLUBS,Card.Rank.JACK),
		new Card(Card.Suit.CLUBS,Card.Rank.QUEEN),
		new Card(Card.Suit.CLUBS,Card.Rank.KING),
		new Card(Card.Suit.DIAMONDS,Card.Rank.ACE),
		new Card(Card.Suit.DIAMONDS,Card.Rank.TWO),
		new Card(Card.Suit.DIAMONDS,Card.Rank.THREE),
		new Card(Card.Suit.DIAMONDS,Card.Rank.FOUR),
		new Card(Card.Suit.DIAMONDS,Card.Rank.FIVE),
		new Card(Card.Suit.DIAMONDS,Card.Rank.SIX),
		new Card(Card.Suit.DIAMONDS,Card.Rank.SEVEN),
		new Card(Card.Suit.DIAMONDS,Card.Rank.EIGHT),
		new Card(Card.Suit.DIAMONDS,Card.Rank.NINE),
		new Card(Card.Suit.DIAMONDS,Card.Rank.TEN),
		new Card(Card.Suit.DIAMONDS,Card.Rank.JACK),
		new Card(Card.Suit.DIAMONDS,Card.Rank.QUEEN),
		new Card(Card.Suit.DIAMONDS,Card.Rank.KING),
		new Card(Card.Suit.HEARTS,Card.Rank.ACE),
		new Card(Card.Suit.HEARTS,Card.Rank.TWO),
		new Card(Card.Suit.HEARTS,Card.Rank.THREE),
		new Card(Card.Suit.HEARTS,Card.Rank.FOUR),
		new Card(Card.Suit.HEARTS,Card.Rank.FIVE),
		new Card(Card.Suit.HEARTS,Card.Rank.SIX),
		new Card(Card.Suit.HEARTS,Card.Rank.SEVEN),
		new Card(Card.Suit.HEARTS,Card.Rank.EIGHT),
		new Card(Card.Suit.HEARTS,Card.Rank.NINE),
		new Card(Card.Suit.HEARTS,Card.Rank.TEN),
		new Card(Card.Suit.HEARTS,Card.Rank.JACK),
		new Card(Card.Suit.HEARTS,Card.Rank.QUEEN),
		new Card(Card.Suit.HEARTS,Card.Rank.KING),		
		new Card(Card.Suit.SPADES,Card.Rank.ACE),
		new Card(Card.Suit.SPADES,Card.Rank.TWO),
		new Card(Card.Suit.SPADES,Card.Rank.THREE),
		new Card(Card.Suit.SPADES,Card.Rank.FOUR),
		new Card(Card.Suit.SPADES,Card.Rank.FIVE),
		new Card(Card.Suit.SPADES,Card.Rank.SIX),
		new Card(Card.Suit.SPADES,Card.Rank.SEVEN),
		new Card(Card.Suit.SPADES,Card.Rank.EIGHT),
		new Card(Card.Suit.SPADES,Card.Rank.NINE),
		new Card(Card.Suit.SPADES,Card.Rank.TEN),
		new Card(Card.Suit.SPADES,Card.Rank.JACK),
		new Card(Card.Suit.SPADES,Card.Rank.QUEEN),
		new Card(Card.Suit.SPADES,Card.Rank.KING)
		));

}
