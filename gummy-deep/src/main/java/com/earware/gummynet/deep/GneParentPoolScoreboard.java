package com.earware.gummynet.deep;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.earware.gummynet.deep.GNEParentPool;
import com.earware.gummynet.deep.GNEParentPool.GneParentStats;

public class GneParentPoolScoreboard {
	GNEParentPool pool;
	GneParentPoolScoreboard(GNEParentPool pool) {this.pool = pool;}
	public String scoreboard() {
		return scoreboardString(pool.selected.size());
	}
	public static class ScoreboardEntry {
		public int count=0;
		public GneParentStats stats;
		ScoreboardEntry(int count, GneParentStats stats) {this.count=count; this.stats=stats;}
	}
	public String scoreboardString(int howFarBack) {
		String rez = "";
		List<ScoreboardEntry> board = scoreboard(howFarBack);
		for (ScoreboardEntry e : board) {
			rez += boardFormat(e);
		}
		return rez;
	}
	public List<ScoreboardEntry> scoreboard(int howFarBack) {
		List<GneParentStats> selected=pool.selected;
		int startNdx = howFarBack<1 ? 0 : 
						howFarBack<selected.size() ? selected.size()-howFarBack :
							0;
		// count how many times each was selected
		HashMap<GneParentStats, Integer> counters = new HashMap<GneParentStats, Integer>();
		// java.util.SortedMap<Integer, String> 
		for (int i=startNdx; i<selected.size(); i++) {
			GneParentStats s = selected.get(i);
			if (counters.get(s)==null) counters.put(s,1);
			counters.put(s,counters.get(s)+1);
		}
		
		// sort 'em
		SortedMap<Integer,List<GneParentStats>> board = new TreeMap<Integer,List<GneParentStats>>();
		for (Map.Entry<GneParentStats, Integer> me : counters.entrySet()) {
			if (!board.containsKey(me.getValue())) board.put(me.getValue(), new ArrayList<GneParentStats>());
			board.get(me.getValue()).add(me.getKey());
		}
		List<ScoreboardEntry> entries = new ArrayList<ScoreboardEntry>();
		for (Integer count : board.keySet()) {
			List<GneParentStats> l = board.get(count);
			for (GneParentStats stats : l) {
				entries.add(new ScoreboardEntry(count, stats));
			}
		}
		return entries;
	}
	
	protected String boardFormat(ScoreboardEntry e) {
		GneParentStats s = e.stats;
		return  e.count
				+ String.format(" score=%2.4f",s.score())
				+ String.format(" winRatio=%2.4f",s.playStats.p1WinRatio())
				+ String.format(" fit=%2.4f",s.playStats.p1Fitness())
				+ String.format(" raw=%2.4f",s.playStats.p1Fitness()*s.playStats.p1WinRatio())
				+ String.format(" numSel=%d",s.numSelections)
				+ String.format(" numProgeny=%d",s.numProgeny())
				+ String.format(" avgProgScore=%2.4f",s.averageProgenyScore())
				+ String.format(" progContrib=%2.4f",s.numSelections<1?0:(((double)s.numProgeny())/((double)s.numSelections))*s.averageProgenyScore())
				+ " " + new File(s.dqnPath).getName()
				+ "\n";
	}
}
