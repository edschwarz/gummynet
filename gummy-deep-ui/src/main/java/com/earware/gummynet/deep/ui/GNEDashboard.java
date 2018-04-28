package com.earware.gummynet.deep.ui;

import java.io.File;
import java.io.StringWriter;

import org.json.*;

import com.earware.gummynet.deep.GNEParentPool;
import com.earware.gummynet.deep.GNEParentPool.GneParentStats;

public class GNEDashboard {
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
	
	GNEParentPool pool;
	public GNEDashboard(GNEParentPool pool) {
		this.pool = pool;
	}
	
	public void start() {
		
	}
	
	public void stop() {
		
	}
	/*
	@Path("/scoreboard")
	public static class Handler {
		@GET
		@Path("/")
		public String getScoreboardSummary() {
			return getScoreboardSummary(0);
		}
		@GET
		@Path("/")
		public String getScoreboardSummary(int howFarBack) {
			
		}
	     @POST
	     @Path("/calc2")
	     public Calculation calculate(Calculation calc) {
	         return doCalc(calc);
	     }
	     private Calculation doCalc(Calculation c) {
	         String op = c.getOperation();
	         int left = c.getLeft();
	         int right = c.getRight();
	         if (op.equalsIgnoreCase("subtract")) {
	             c.setResult(left - right);
	         } else if (op.equalsIgnoreCase("multiply")) {
	             c.setResult(left * right);
	         } else if (op.equalsIgnoreCase("divide")) {
	             c.setResult(left / right);
	         } else {
	             c.setResult(left + right);
	         }
	         return c;
	     }
	 }
	 */
}

