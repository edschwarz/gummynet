package com.earware.gummynet.deep.ui;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.ResourceConfig;

import com.earware.gummynet.deep.GNE;
import com.earware.gummynet.deep.GummyNetworkEvolver;

public class GNEDashboard extends ResourceConfig {
	final GummyNetworkEvolver evolver = new GummyNetworkEvolver();
	public GNEDashboard(@Context ServletContext servletContext) {
		packages("com.earware.gummynet.deep.ui");
		String[] args = (String[])(servletContext.getAttribute("args"));
		GummyNetworkEvolver.Config gneConfig = GNE.processCommandLine(args);
		startGNE(gneConfig);
	}
		
    private static GummyNetworkEvolver startGNE(GummyNetworkEvolver.Config gneConfig) {
		final GummyNetworkEvolver evolver = new GummyNetworkEvolver();
		new Thread(new Runnable() {public void run() {
    		try {
    			GummyNetworkEvolver.Stats evolvedStats 
    					= new GummyNetworkEvolver().evolve(gneConfig);
            		
    			if (evolvedStats!=null) {
    				//LOGGER.info("main: winner: " + evolvedStats.toString());
    			} else {
    				//LOGGER.info("main: no survivors");
    			}
    		} catch (Exception e) {
    				e.printStackTrace();
    		}
		}}).start();
		
		return evolver; 
    }
    
    GummyNetworkEvolver getEvolver() {return evolver;}	
}
