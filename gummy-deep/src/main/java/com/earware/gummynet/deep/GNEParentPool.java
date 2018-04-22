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

class GNEParentPool {
	public static class GneParentStats implements Comparable<GneParentStats> {
		String dqnPath;
		public GummyDeepPlayGin.Stats playStats=null;
		public ArrayList<GneParentStats> progenyStats = new ArrayList<GneParentStats>();
		public GneParentStats(String dqnPath, GummyDeepPlayGin.Stats playStats) {
			this.dqnPath= dqnPath; 
			this.playStats=playStats;
		}
		
		public double score() {
			double score =  playStats.p1WinRatio() * playStats.p1Fitness();
			return score;
		}
		public double averageProgenyScore() {
			double totScore=0; 
			for (GneParentStats stats : progenyStats) totScore+=stats.score();
			return totScore/progenyStats.size();
		}
		
		@Override
		public int compareTo(GneParentStats o) {
			if (o==null) return 0;
			return Double.compare(score(), o.score());
		}
		@Override
		public String toString() {
			return 		"dqnPath=" + dqnPath
					+ "  score=" + score()
					+ "  numProgeny=" + progenyStats.size()
					+ "  averageProgenyScore=" + averageProgenyScore()
					+ "  playStats=" + playStats.toString();
		}
	}
	private class PoolEntry implements Comparable<PoolEntry> {
		GneParentStats stats;
		ArrayList<PoolEntry> progeny = new ArrayList<PoolEntry>();
		
		String dqnPath() {return stats.dqnPath;}
		public PoolEntry(GneParentStats stats) {
			this.stats = stats;
		}
		@Override
		public int compareTo(PoolEntry o) {
			if (o==null) return 0;
			return stats.compareTo(o.stats);
		}
		@Override
		public String toString() {
			return stats.toString();
		}
	}
	
	public File poolDir;
	List<PoolEntry> pool = new ArrayList<PoolEntry>(); 
	int activePoolSize = 10;
	
	GNEParentPool(File poolDir) {this.poolDir = poolDir;}
	
	/** select from top "activePoolSize" entries, with choice weighted by score */ 
	public String getParentFromPool() throws IOException {
		LOGGER.info("SELECTING from top " + activePoolSize 
				+ " out of pool of " + pool.size() + ": " + pool.toString());
		if (pool.size()==0) {
			return null;
		}
		sortPool();
		PoolEntry parentEntry=null;
		double totalScore=0;
		for (int i=0; i<activePoolSize && i<pool.size(); i++) {
			PoolEntry candidateEntry = pool.get(i);
			totalScore += candidateEntry.stats.score();
		}
		double choiceVal = Math.random()*totalScore;
		totalScore=0;
		for (int i=0; i<activePoolSize && i<pool.size(); i++) {
			parentEntry = pool.get(i);
			totalScore += parentEntry.stats.score();
			if (totalScore >= choiceVal) {
				break;
			}
		}
		confirmPoolFile(parentEntry);
		LOGGER.info("SELECTED: " + parentEntry.toString());
		return parentEntry.dqnPath();
	}
	
	public void addProgeny(String dqnPath, GummyDeepPlayGin.Stats playStats, String parentDqnPath) {
		PoolEntry newPE = new PoolEntry(new GneParentStats(dqnPath, playStats));
		for (PoolEntry pe : pool) {
			if (pe.dqnPath().equals(parentDqnPath)) {
				pe.progeny.add(newPE);
				pe.stats.progenyStats.add(newPE.stats);
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
		pool.add(new PoolEntry(new GneParentStats(dqnPath, playStats)));
	}
	
	public List<GneParentStats> getStatsList() {
		ArrayList<GneParentStats> rez = new ArrayList<GneParentStats>();
		for (PoolEntry pe : pool) {
			rez.add(pe.stats);
		}
		Collections.sort(rez);
		Collections.reverse(rez);
		return rez;
	}

	private static int poolNdx = 1; 
	private void confirmPoolFile(PoolEntry pe) throws IOException {
		if (pe==null || pe.dqnPath()==null) {
			return;
		}
		File sourceFile = new File(pe.dqnPath());
		File sourceDir = sourceFile.getParentFile();
		if (!poolDir.equals(sourceDir)) {
			String targetFilename = sourceFile.getName() + ".pool." + poolNdx++; 
			Path targetPath = Files.copy(sourceFile.toPath(), 
					new File(poolDir, targetFilename).toPath(), 
					StandardCopyOption.REPLACE_EXISTING);
			pe.stats.dqnPath = targetPath.toString();
		}
	}	
    protected static Logger LOGGER = Logger.getLogger(GummyNetworkEvolver.class.getName()); 
}
