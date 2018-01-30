package com.earware.gummynet.deep;

import java.util.logging.Logger;

import com.earware.gummynet.gin.GinHand;
import com.earware.gummynet.gin.Player;

public class GummySimulator {
    final private static int STATS_REPORTING_FREQUENCY=10;
    final private static int LATCH_SLEEP=10;
    final static int STEPS_MIN_FOR_NEW=0;
    
	static class GummySim {
		Player player;
		GummyMDP mdp;
	}
	
	private GummySim simOne;
	private GummySim simTwo;
	private GummyState gummyState;
	private Player playerOne = new Player("joe");
	private Player playerTwo = new Player("annie");
	
	private GummyState.InputWeights inputWeights;
	
	public GummySimulator(GummyState.InputWeights inputWeights, String player1Name, String player2Name) {
		playerOne = new Player(player1Name);
		playerTwo = new Player(player2Name);
		this.inputWeights = inputWeights;
	}
	
	public GummySimulator(String player1Name, String player2Name) {
		this(new GummyState.InputWeights(), player1Name, player2Name);
	}
	
	public GummySimulator() {
		this(new GummyState.InputWeights(), "joe", "annie");
	}
	
	public void close() {
		// TODO Auto-generated method stub
	}

	public void init(GummyMDP mdp1, GummyMDP mdp2) {
		simOne = new GummySim();
		simOne.player = this.playerOne;
		simOne.mdp = mdp1; 
		simTwo = new GummySim();
		simTwo.player = this.playerTwo;
		simTwo.mdp = mdp2; 
		this._currentlyRunningMDP = simTwo.mdp;
		this.reInit();
	}

    private GummyState reInit() {
		if (stats.getNumHandsCompleted()%STATS_REPORTING_FREQUENCY==0 && 
				stats.getNumHandsCompleted()>0) {
			LOGGER.info(stats.toString());
		}
		synchronized(this) {
			gummyState = reInitGummyState();
		}
		return gummyState;
    }
    
    private GummyState reInitGummyState() {
		GinHand ginHand = new GinHand(playerOne, playerTwo);
		ginHand.deal();
		GummyState state = new GummyState(ginHand);
		state.setInputWeights(inputWeights);
		return state;
    }
    
    public GummySim getSimOne() {return simOne;}
    public GummySim getSimTwo() {return simTwo;}
    
    /*
     *  
     */
	private GummyState newGummyState(GummyMDP requestor) {
		if (gummyState == null || gummyState.stepCount>STEPS_MIN_FOR_NEW-1) {
			return reInit();
		}
		if (gummyState != null && gummyState.stepCount>0) {
			LOGGER.warning("new state requested, count is: " + gummyState.stepCount);	
		}
		return gummyState; 
	} 
	
    /////////////////////////////////////////////
    // interface for MDPs
	GummyState killThisBadThing(GummyMDP requestor) {return newGummyState(requestor);}
	
	GummyState getGummyState() {return gummyState;}
	
	GummySimulatorStats getStats() {return stats;}
	
	GummyState reset(GummyMDP requestor) {
		return _resetLatch(requestor);
	}	
	void latch(GummyMDP mdp) {
		this._latch(mdp);
		while (_currentlyRunningMDP!=mdp && _currentlyRunningMDP!=null) {
			try {Thread.sleep(LATCH_SLEEP);} catch (InterruptedException e) {return;}
		}
	}
	
	void latchFinal(GummyMDP mdp) {
		//LOGGER.info("latchFinal called, cleaning up.");
		this._latch(null);
	}
	
	void latchDone(GummyMDP mdp) {
		_latchedForDone = true;
		this.latch(mdp);
		_latchedForDone = false;
	}
	
	/////  latch implementation
	private GummyMDP _currentlyRunningMDP;
	private boolean _latchedForDone = false;
	private synchronized void _latch(GummyMDP mdp) {
		if (_currentlyRunningMDP==null) {
			return; // finshed, let all threads drain
		}
		GummyMDP next = null; 
		if (mdp==simOne.mdp) {
			if (simTwo.mdp.needsLatch()) {
				next = simTwo.mdp;
			} else {
				if (!(simOne.mdp.isDone())) {
					simTwo.mdp.step(0); // draw
				}
				next = mdp;
			}
		} else if (mdp==simTwo.mdp) {
			if (simOne.mdp.needsLatch()) {
				next = simOne.mdp;
			} else {
				if (!(simTwo.mdp.isDone())) {
					simOne.mdp.step(0); // draw
				}
				next = mdp;
			}
		} else if (mdp==null) {
			next = null;
		} else {
			throw new IllegalArgumentException("unknown MDP in latch()");
		}
		
		_currentlyRunningMDP = next;
	}
	
	private GummyState _resetLatch(GummyMDP requestor) {
		if (_latchedForDone) {
			latch(requestor);
			return gummyState;
		} 
		return newGummyState(requestor);
	}
	
	// logging
	private GummySimulatorStats stats = new GummySimulatorStats();
    public static Logger LOGGER = Logger.getLogger(GummyMDP.class.getPackage().getName()); 
}
