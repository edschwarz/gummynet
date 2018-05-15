package com.earware.gummynet.deep;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class GNEParentPool {
	
	public static class GneParentStats implements Comparable<GneParentStats> {
		public String dqnPath;
		public GummyDeepPlayGin.Stats playStats=null;
		public ArrayList<GneParentStats> progenyStats = new ArrayList<GneParentStats>();
		public int numSelections=0;
		public long poolEntryTime=System.currentTimeMillis();
		
		public GneParentStats(String dqnPath, GummyDeepPlayGin.Stats playStats) {
			this.dqnPath= dqnPath; 
			this.playStats=playStats;
		}
		
		public double score() {
			double score =  playStats.p1WinRatio() * playStats.p1Fitness();
			if (numProgeny()>0) {
				score += progenyRatio() * averageProgenyScore();
			}
			return score;
		}
		
		public double averageProgenyScore() {
			if (progenyStats.size()==0) return 0;
			double totScore=0; 
			for (GneParentStats stats : progenyStats) totScore+=stats.score();
			return totScore/progenyStats.size();
		}
		
		public double progenyRatio() {
			return numSelections>0?((double)numProgeny())/((double)numSelections):0.0;
		}
		
		public int numProgeny() {
			return progenyStats.size();
		}
		
		public long msecInPool() {
			return System.currentTimeMillis() - this.poolEntryTime;
		}
		
		@Override
		public int compareTo(GneParentStats o) {
			if (o==null) return 0;
			return Double.compare(score(), o.score());
		}
		@Override
		public String toString() {
			return 		"dqnPath=" + dqnPath
					+ "  score=" + String.format("%3.4f", score())
					+ "  numSelections=" + numSelections
					+ "  numProgeny=" + numProgeny() + " " + String.format("(%3.4f%%)", progenyRatio())
					+ "  averageProgenyScore=" + String.format("%3.4f", averageProgenyScore())
					+ "  playStats=" + playStats.toString();
		}
	}
	
	// *********************************************
	public File poolDir;
	List<GneParentStats> pool = new ArrayList<GneParentStats>(); 
	int activePoolSize = 10;
	List<GneParentStats> selected = new ArrayList<GneParentStats>();
	private int poolNdx = 1; 
	GneParentPoolScoreboard scoreboard=null;
	
	// *********************************************
	GNEParentPool(File poolDir) {this.poolDir = poolDir;}
	
	/** select from top "activePoolSize" entries, with choice weighted by score */ 
	public String getParentFromPool() throws IOException {
		//LOGGER.info("SELECTING from top " + activePoolSize 
		//		+ " out of pool of " + pool.size() + ": " + pool.toString());
		if (pool.size()==0) {
			return null;
		}
		sortPool();
		GneParentStats parentStats=null;
		double totalScore=0;
		for (int i=0; i<activePoolSize && i<pool.size(); i++) {
			GneParentStats candidateStats = pool.get(i);
			totalScore += candidateStats.score();
		}
		double choiceVal = Math.random()*totalScore;
		double selectionTotal=0;
		boolean broke=false;
		for (int i=0; i<activePoolSize && i<pool.size(); i++) {
			parentStats = pool.get(i);
			selectionTotal += parentStats.score();
			if (selectionTotal >= choiceVal) {
				broke=true;
				break;
			}
		}
		
		if (!broke) {
			LOGGER.warning("SELECT ISSUE: did not exit via break.");
		}
		confirmPoolFile(parentStats);
		parentStats.numSelections++;
		selected.add(parentStats);
		LOGGER.info("SELECTED: " 
							+ String.format("@%2.4f/%2.4f/%2.4f", choiceVal, selectionTotal, totalScore) 
							+ " -- "  + parentStats.toString());
		return parentStats.dqnPath;
	}
	
	public void addProgeny(String dqnPath, GummyDeepPlayGin.Stats playStats, String parentDqnPath) {
		if (pool.size()==0) {
			return;
		}
		GneParentStats newPE = new GneParentStats(dqnPath, playStats);
		for (GneParentStats pe : pool) {
			if (pe.dqnPath.equals(parentDqnPath)) {
				pe.progenyStats.add(newPE);
			}
		}
		pool.add(newPE);
		balancePool();
	}
	
	public void balancePool() {
		sortPool();
		while (pool.size()>activePoolSize*2) {
			pool.remove(pool.size()-1);
		}
	}
	
	protected void sortPool() {
		Collections.sort(pool);
		Collections.reverse(pool);
	}
	
	public void addParent(String dqnPath, GummyDeepPlayGin.Stats playStats) {
		pool.add(new GneParentStats(dqnPath, playStats));
	}
	
	// a copy of the list, the actual stats tho
	public List<GneParentStats> getStatsList() {
		ArrayList<GneParentStats> rez = new ArrayList<GneParentStats>();
		for (GneParentStats pe : pool) {
			rez.add(pe);
		}
		Collections.sort(rez);
		Collections.reverse(rez);
		return rez;
	}
	
	private void confirmPoolFile(GneParentStats pe) throws IOException {
		if (pe==null || pe.dqnPath==null) {
			return;
		}
		File sourceFile = new File(pe.dqnPath);
		File sourceDir = sourceFile.getParentFile();
		if (!poolDir.equals(sourceDir)) {
			String targetFilename = sourceFile.getName() + ".pool." + poolNdx++; 
			Path targetPath = Files.copy(sourceFile.toPath(), 
					new File(poolDir, targetFilename).toPath(), 
					StandardCopyOption.REPLACE_EXISTING);
			pe.dqnPath = targetPath.toString();
		}
	}	
	
	GneParentPoolScoreboard getScoreboard() {
		if (scoreboard==null) {
			scoreboard = new GneParentPoolScoreboard(this);
		}
		return scoreboard;
	}
	
    protected static Logger LOGGER = Logger.getLogger(GummyNetworkEvolver.class.getName()); 
}
