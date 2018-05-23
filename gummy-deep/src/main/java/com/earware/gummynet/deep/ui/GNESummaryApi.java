package com.earware.gummynet.deep.ui;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.json.*;

import com.earware.gummynet.deep.GummyNetworkEvolver;

@Path("/summary")
public class GNESummaryApi {
	public static class SummaryWrapper {
		GummyNetworkEvolver.Stats e;
		public String getSummary() {return e.toString();}

		public String toJSON() {
			StringWriter w = new StringWriter();
			JSONObject job = new JSONObject(this);
			job.write(w);
			w.flush();
			return w.toString();
		}
		
		SummaryWrapper(GummyNetworkEvolver.Stats e) {this.e=e;}
		public static String toJSONString(GummyNetworkEvolver.Stats s) {return new SummaryWrapper(s).toJSON();}
	}
	
	private GummyNetworkEvolver evolver() {return GummyNetworkEvolver.getLatestInstance();}
	public GNESummaryApi() {
		//this.evolver = GNERestServer.getEvolver();
	}
	
	private static final String NOT_AVAILABLE_RESPONSE = "{\"summary\": \"no summary\"}"; 
	@GET
	@Path("/")
	public String getSummary() {
		GummyNetworkEvolver evolver = evolver();
		if (evolver!=null) {
			String rez="";
			GummyNetworkEvolver.Stats s = evolver.getStats();
			if (s!=null) {
				rez += SummaryWrapper.toJSONString(s);
			} else {
				rez += NOT_AVAILABLE_RESPONSE; 
			}
			return rez;
		} 
		return NOT_AVAILABLE_RESPONSE;
	}	
    protected static Logger LOGGER = Logger.getLogger(GNEDashboard.class.getName()); 
}

