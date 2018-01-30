package com.earware.gummynet.gin;

public class Move {
	final Player player;
	Draw draw = null;
	Card discard = null;
	
	public static class Draw {
		public enum Source {PILE, DECK};
		public final Source source;
		public final Card card;
		
		public Draw(Source source, Card card) {
			this.source=source;
			this.card=card;
		}
	}
	
	public Move(Player player) {
		this.player = player;
	}
	
	public Card getDiscard() {return discard;}
	public Draw getDraw() {return draw;}
	public Player getPlayer() {return player;}
	
	public String toString() {
		String rez = "";
		Move.Draw.Source drawSource = this.draw.source;
		String drawCardStr = (drawSource==Move.Draw.Source.PILE) ?
				this.draw.card.toString() + " " : "";
		rez += "opponent drew " + drawCardStr + "from the " + drawSource + '\n';
		rez += "opponent discarded " + this.getDiscard().toString();
		return rez;
	}
}

