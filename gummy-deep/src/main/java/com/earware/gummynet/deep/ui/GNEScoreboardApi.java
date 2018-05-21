package com.earware.gummynet.deep.ui;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.json.*;

import com.earware.gummynet.deep.GneParentPoolScoreboard;
import com.earware.gummynet.deep.GummyNetworkEvolver;
import com.earware.gummynet.deep.GneParentPoolScoreboard.ScoreboardEntry;

@Path("/GNEScoreboardApi")
public class GNEScoreboardApi {
	public static class ScoreboardEntryWrapper {
		ScoreboardEntry e;
		public double getCount() {return e.count;} 
		public double getScore() {return e.stats.score();} 
		public double getWinRatio() {return e.stats.playStats.p1WinRatio();}
		public double getFitness() {return e.stats.playStats.p1Fitness(); }
		public double getRaw() {return e.stats.playStats.p1Fitness()*e.stats.playStats.p1WinRatio();}
		public int    getNumSelected() {return e.stats.numSelections;}
		public int    getNumProgeny() {return e.stats.numProgeny();};
		public double getAvgProgenyScore() {return e.stats.averageProgenyScore();}
		public double getProgenyContrib() {return e.stats.numSelections<1?0:(((double)e.stats.numProgeny())/((double)e.stats.numSelections))*e.stats.averageProgenyScore();}
		public String getDqnName() {return new File(e.stats.dqnPath).getName();}
		public long getAge() {return e.stats.msecInPool()/1000;}

		public String toJSON() {
			StringWriter w = new StringWriter();
			JSONObject job = new JSONObject(this);
			job.write(w);
			w.flush();
			return w.toString();
		}
		
		ScoreboardEntryWrapper(ScoreboardEntry e) {this.e=e;}
		public static String toJSONString(ScoreboardEntry s) {return new ScoreboardEntryWrapper(s).toJSON();}
	}
	
	private GummyNetworkEvolver evolver() {return GummyNetworkEvolver.getLatestInstance();}
	public GNEScoreboardApi() {
		//this.evolver = GNERestServer.getEvolver();
	}
	
	@GET
	@Path("/")
	public String getScoreboardSummary() {
		return getScoreboardSummary(0);
	}
	
	@GET
	@Path("/{howFarBack}")
	public String getScoreboardSummary(@PathParam("howFarBack") int howFarBack) {
		return _getScoreboardSummary(howFarBack);
	}
	
	private static final String NO_MODELS_RESPONSE = "[{\"score\": \"no models yet\"}]"; 
	private String _getScoreboardSummary(int howFarBack) {
		GummyNetworkEvolver evolver = evolver();
//		if (evolver.getStats()!=null) {
		//	rez += evolver.getStats().toString() + "\n";
//		}
		if (evolver!=null) {
			String rez="";
			GneParentPoolScoreboard s = evolver.getScoreboard();
			if (s!=null) {
				List<ScoreboardEntry> entries = s.scoreboard(howFarBack);
				if (entries.size()>0) {
					rez = "[";
					for (ScoreboardEntry e : entries) {
						rez += ScoreboardEntryWrapper.toJSONString(e);
						rez += ",";
					}
					// strip last comma
					rez = rez.substring(0, rez.length()-1);
					rez += "]";
				} else {
					rez += NO_MODELS_RESPONSE; 
				}
			}
			return rez;
		} 
		return NO_MODELS_RESPONSE;
	}	
    protected static Logger LOGGER = Logger.getLogger(GNEDashboard.class.getName()); 
}

