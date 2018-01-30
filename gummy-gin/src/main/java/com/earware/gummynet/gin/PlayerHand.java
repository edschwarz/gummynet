package com.earware.gummynet.gin;

import java.util.*;

public class PlayerHand {
	private List<Card> cards;
	public boolean wins() {
		return GinTester.wins(this);
	}
	
	public List<Card> getCards() {return cards;}
	public String toString() {
		StringBuffer sb = new StringBuffer(); 
		for (Card card : getCards()) {
			sb.append(card.toString());
			sb.append(' ');
		}
		return sb.toString(); 
	}
	
	public PlayerHand(List<Card> cards) {
		this.cards = cards;
	}
	
	public PlayerHand copy() {
		List<Card> cardsCopy = new ArrayList<Card>(this.cards.size());
		for (Card c : this.cards) {
			cardsCopy.add(c);
		}
		return new PlayerHand(cardsCopy);
	}
	
	public boolean contains(Card card) {
		return cards.contains(card);
	}
	
	public boolean equals(Object o) {
		if (o instanceof PlayerHand) {
			PlayerHand other = (PlayerHand)o;
			for (Card card : other.cards) {
				if (!this.contains(card)) {
					return false;
				}
				return true;
			}
		}
		return false;
	}
}

