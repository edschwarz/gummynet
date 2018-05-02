package com.earware.gummynet.deep.ui;

import java.io.File;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.json.*;

import com.earware.gummynet.deep.GNEParentPool.GneParentStats;
import com.earware.gummynet.deep.GummyNetworkEvolver;

@Path("/GNEScoreboardApi")
public class GNEScoreboardApi {
	private static class GneParentStatsBeanWrapper {
		GneParentStats s;
		public double getScore() {return s.score();} 
		public double getWinRatio() {return s.playStats.p1WinRatio();}
		public double getFitness() {return s.playStats.p1Fitness(); }
		public double getRaw() {return s.playStats.p1Fitness()*s.playStats.p1WinRatio();}
		public int    getNumSelected() {return s.numSelections;}
		public int    getNumProgeny() {return s.numProgeny();};
		public double getAvgProgenyScore() {return s.averageProgenyScore();}
		public double getProgenyContrib() {return s.numSelections<1?0:(s.numProgeny()/s.numSelections)*s.averageProgenyScore();}
		public String getDqnName() {return new File(s.dqnPath).getName();}

		public String toJSON() {
			StringWriter w = new StringWriter();
			new JSONObject(this).write(w);
			return w.toString();
		}
		
		GneParentStatsBeanWrapper(GneParentStats s) {this.s=s;}
		public static String toJSONString(GneParentStats s) {return new GneParentStatsBeanWrapper(s).toJSON();}
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
	
	@PUT
	@Path("/{howFarBack}")
	public String getScoreboardSummary(int howFarBack) {
		return "<html><head/><body><pre>" +
				_getScoreboardSummary(howFarBack)
				+ "</pre></body></html>";
	}
	
	private String _getScoreboardSummary(int howFarBack) {
		GummyNetworkEvolver evolver = evolver();
		if (evolver!=null) {
			String rez = "";
			if (evolver.getStats()!=null) {
				rez += evolver.getStats().toString() + "\n";
			}
			rez += evolver.getScoreboardString(howFarBack);
			return rez;
		} 
		return "no stats list";
	}
}

