package com.earware.gummynet.gin;

public interface GinStrategy {
	Move.Draw.Source getDrawSource(GinHand hand);
	Card getDiscardCard(PlayerHand playerHand, GinHand hand);
}

