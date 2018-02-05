package com.earware.gummynet.gin;

import java.util.*;

import com.earware.gummynet.gin.Move.Draw;

public class GinHand {
	Deck deck;
	Card discard;
	List<Move> moves;
	Playing playingOne;
	Playing playingTwo;
	Playing currentlyPlaying;

	public enum ActionType {DRAW, DISCARD};

	public static class Playing {
		private Player player;
		private PlayerHand playerHand;
		GinStrategy strategy;
		
		public Playing copy() {
			Playing p=new Playing();
			p.setPlayer(this.getPlayer());
			p.strategy=this.strategy;
			if (this.getPlayerHand()!=null) {
				p.setPlayerHand(new PlayerHand(new ArrayList<Card>(this.getPlayerHand().getCards())));
			}
			return p;
		}

		public Player getPlayer() {
			return player;
		}

		public void setPlayer(Player player) {
			this.player = player;
		}

		public PlayerHand getPlayerHand() {
			return playerHand;
		}

		public void setPlayerHand(PlayerHand playerHand) {
			this.playerHand = playerHand;
		}
	}
		
	public Player getPlayerOne() {return playingOne.getPlayer();}
	public PlayerHand getPlayerHandOne() {return playingOne.getPlayerHand();}
	public Player getPlayerTwo() {return playingTwo.getPlayer();}
	public PlayerHand getPlayerHandTwo() {return playingTwo.getPlayerHand();}
	public int getCurrentPlayerIndex() {return currentlyPlaying==playingOne?0:1;}
	public Player getCurrentPlayer() {return currentlyPlaying.getPlayer();}
	public PlayerHand getCurrentPlayerHand() {return currentlyPlaying.getPlayerHand();}
	public Card getDiscard() {return discard;}
	public boolean isDeckEmpty() {return deck.isEmpty();}
	public Deck getDeck() {return deck;}  // really just for test support

	public Player getPlayer(String name) {
		if (playingOne.player.name.equals(name)) {
			return playingOne.getPlayer();
		} else if (playingTwo.player.name.equals(name)) {
			return playingTwo.getPlayer();
		}
		return null;
	}
	
	public PlayerHand getPlayerHand(String name) {
		if (playingOne.player.name.equals(name)) {
			return playingOne.getPlayerHand();
		} else if (playingTwo.player.name.equals(name)) {
			return playingTwo.getPlayerHand();
		}
		return null;
	}
	
	public Move getLastMove() {
		if (moves == null || moves.size()==0) {
			return null;
		}
		return moves.get(moves.size()-1);
	}
	
	public List<Move> getMoves() {
		return moves;
	}
	
	public GinHand(Player playerOne, Player playerTwo) {
		this(playerOne, null, playerTwo, null);
	}
	public GinHand(Player playerOne, GinStrategy strategyOne, Player playerTwo, GinStrategy strategyTwo) {
		playingOne = new Playing();
		playingOne.setPlayer(playerOne);
		playingOne.strategy = strategyOne;	
		
		playingTwo = new Playing();
		playingTwo.setPlayer(playerTwo);
		playingTwo.strategy = strategyTwo;	
		
		deck = new Deck();
		deck.shuffle();
		
		moves = new ArrayList<Move>();
		
	}
	
	public void deal() {
		deck.shuffle();
		playingOne.setPlayerHand(dealHand());
		playingTwo.setPlayerHand(dealHand());
		
		discard = deck.next();
		currentlyPlaying = playingOne;
		
		Move move = new Move(currentlyPlaying.getPlayer());
		moves.add(move);
	}
	
	private PlayerHand dealHand() {
		List<Card> cards = new ArrayList<Card>();
		for (int i=0; i<7; i++) {
			cards.add(deck.next());
		}
		return new PlayerHand(cards);
	}
	
	private Card drawFromDeck() {return deck.next();}
	private Card drawFromDiscard() {return getDiscard();}
	
	/** 
	 * drain the deck 
	 */
	public void drain() {
		while (drawFromDeck()!=null);
	}
	
	public Playing play() {
		return play(Integer.MAX_VALUE);
	}
	
	public Playing play(int maxTurns) {
		Playing winner = null; 
		int turns = 0;
		
		while (true) {
			
			if (turns>=maxTurns) {
				break;
			}
			turns++;
			
			// for now, bail if we get to the bottom of the deck
			if (deck.getUndealt().size()==0) {
				break;
			}
					
			Move.Draw.Source drawSource = currentlyPlaying.strategy.getDrawSource(this);
			pushDraw(drawSource);
			
			if (currentlyPlaying.getPlayerHand().wins()) {
				winner = currentlyPlaying;
				break;
			}

			Card discardCard = currentlyPlaying.strategy.getDiscardCard(currentlyPlaying.getPlayerHand(), this);
			pushDiscard(discardCard);
		}	
		return winner;
	}
	
	public void pushDraw(Draw.Source drawSource) {
		Card drawCard;
		if (drawSource.equals(Move.Draw.Source.DECK)) {
			drawCard = drawFromDeck();
		} else {
			drawCard = drawFromDiscard(); 
		}
		currentlyPlaying.getPlayerHand().getCards().add(drawCard);
		getLastMove().draw = new Move.Draw(drawSource, drawCard);
	}
	
	public void pushDiscard(Card discardCard) {
		discard = discardCard;
		currentlyPlaying.getPlayerHand().getCards().remove(getDiscard());
		getLastMove().discard = getDiscard();
		
		if (currentlyPlaying.equals(playingOne)) {
			currentlyPlaying = playingTwo;
		} else {
			currentlyPlaying = playingOne;
		}
		
		Move move = new Move(currentlyPlaying.getPlayer());
		moves.add(move);
	}
	
	public ActionType getPendingAction() {
		Move move = getLastMove();
		if (move == null || move.draw == null) {
			return ActionType.DRAW;
		} else {
			return ActionType.DISCARD;
		}
	}
	
	/*
	 * Players are not copied, since they don't change over the course of the game
	 */
	public GinHand copy() {
		GinHand hand = new GinHand(this.getPlayerOne(), this.playingOne.strategy, this.getPlayerTwo(), this.playingTwo.strategy );
		
		if (this.playingOne.getPlayerHand()!=null) {
			hand.playingOne.setPlayerHand(new PlayerHand(new ArrayList<Card>(this.playingOne.getPlayerHand().getCards())));
			hand.playingTwo.setPlayerHand(new PlayerHand(new ArrayList<Card>(this.playingTwo.getPlayerHand().getCards())));
		}
		
		if (this.currentlyPlaying!=null) {
			if (this.currentlyPlaying.equals(this.playingOne)) {
				hand.currentlyPlaying = hand.playingOne;
			} else {
				hand.currentlyPlaying = hand.playingTwo;			
			}
		}
		
		if (hand.deck != null) {
			hand.deck = this.deck.copy();
		}
		
		hand.discard = this.discard;
		if (moves!=null) {
			hand.moves = new ArrayList<Move>(moves);
		}
		
		return hand;
	}
	
	public Object clone() {
		return copy();
	}
	
	public String toString() {
		String rez = "";
		
		if (moves != null && moves.size()>1) {
			Move m = moves.get(moves.size()-2);
			rez += m.toString();
		} else {
			rez += "first play";
		}
		
		rez += '\n';
		
		rez += toString(playingOne) + '\n';
		rez += toString(playingTwo) + '\n';
		
		return rez;
	}
	
	public String toString(int which) {
		return toString(which==1?playingOne:playingTwo);
	}
	
	private String toString(Playing playing) {
		String rez = "";
		
		rez += playing.getPlayer().name;
		if (playing.getPlayerHand()!=null) {
			rez += "'s hand: " 
					+ playing.getPlayerHand().toString(); 
		}
		
		if (getDiscard()!=null) {
			rez += "     PILE: " + getDiscard();
		}
		
		return rez;
	}
}
