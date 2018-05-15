package com.earware.gummynet.deep.ui;

import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.ResourceConfig;

import com.earware.gummynet.deep.GNE;
import com.earware.gummynet.deep.GummyNetworkEvolver;

public class GNEDashboard extends ResourceConfig {
	GummyNetworkEvolver evolver = null;
	private long initTime=System.currentTimeMillis();

	public GNEDashboard(@Context ServletContext servletContext) {
		LOGGER.info("GNEDashboard: webapp initialized by container at " + new Date());

		LOGGER.info("GNEDashboard: scanning for rest services...");
		packages("com.earware.gummynet.deep.ui");
		LOGGER.info("GNEDashboard: scanning for rest services completed");
		
		Object o = servletContext.getAttribute("args");
		if (o!=null) {
			String[] args = (String[])(o);
			LOGGER.info("GNEDashboard: retrieved args: " + args);
		
			LOGGER.info("GNEDashboard: starting GummyNetworkEvolver...");
			GummyNetworkEvolver.Config gneConfig = GNE.processCommandLine(args);
			evolver = startGNE(gneConfig);
			LOGGER.info("GNEDashboard: started GummyNetworkEvolver");
		} else {
			Object oo = servletContext.getAttribute("gummyNetworkEvolver");
			if (oo!=null) {
				evolver = (GummyNetworkEvolver)(o);
				LOGGER.info("GNEDashboard: retrieved GummyNetworkEvolver: " + evolver);
			}
		}		
		LOGGER.info("GNEDashboard: scanning for rest services completed");
	}
		
    private static GummyNetworkEvolver startGNE(GummyNetworkEvolver.Config gneConfig) {
		final GummyNetworkEvolver evolver = new GummyNetworkEvolver();
		new Thread(new Runnable() {public void run() {
    		try {
    			GummyNetworkEvolver.Stats evolvedStats 
    					= new GummyNetworkEvolver().evolve(gneConfig);
            		
    			if (evolvedStats!=null) {
    				LOGGER.info("GNEDashboard: winner: " + evolvedStats.toString());
    			} else {
    				LOGGER.info("GNEDashboard: no survivors");
    			}
    		} catch (Exception e) {
    				e.printStackTrace();
    		}
		}}).start();
		
		return evolver; 
    }
    
    GummyNetworkEvolver getEvolver() {return evolver;}	
    protected static Logger LOGGER = Logger.getLogger(GNERestServer.class.getName()); 
}
