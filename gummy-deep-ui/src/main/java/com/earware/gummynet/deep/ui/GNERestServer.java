	package com.earware.gummynet.deep.ui;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.StdErrLog;

public class GNERestServer {
	private static String LOGGER_NAME = GNERestServer.class.getName();
	private static String WEBAPP_RESOURCES_LOCATION = "/webapp.war";
	public static void main( String[] args ) 
    {
    	new GNERestServer().runServer(args);
    }
    public void runServer(String[] args) {	
    	runServer( 8080,"args", args);
    }
        
    public GNERestServer(int port, Object gummyNetworkEvolver) {
    	runRestServer(port, gummyNetworkEvolver);
    }
    
	// -------------------------------------
	private long startTime = System.currentTimeMillis();

	private GNERestServer() {}
	
    private void runRestServer(int port, Object gummyNetworkEvolver) {
		new Thread(new Runnable() {public void run() {
			runServer(8080,gummyNetworkEvolver);
		}}).start();
    }    
        	
    private void runServer(int port, Object gummyNetworkEvolver) {	
    	runServer(port, "gummyNetworkEvolver", gummyNetworkEvolver);
    }
    
    private void runServer(int port, String attributeName, Object attributeValue) {
    	Server server = null;
    	try {
    		Log.setLog(new StdErrLog());
    		LOGGER.info("Rest Server Startup at " + new Date());

    		LOGGER.info("Rest Server: creating Server...");
	        server = new Server(port);
    		LOGGER.info("Rest Server: creating Server completed in " + elapsed() + " msec");
	        
    		LOGGER.info("Rest Server: configuring webapp gummy-rest...");
	        WebAppContext context = configureWebapp();
            context.setAttribute(attributeName, attributeValue);
    		LOGGER.info("Rest Server: configuring webapp gummy-rest completed in " + elapsed() + " msec");

            server.setHandler(context);
	        
    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
    		LOGGER.info("Rest Server: starting Server...");
	        server.start();
    		LOGGER.info("Rest Server: starting Server completed in  " + elapsed() + " msec");

	        server.dumpStdErr();
    	} catch (Exception e) {
    		LOGGER.info("Rest Server: exception during UI startup: " + e.getLocalizedMessage() );
    		e.printStackTrace();
    	}
    	
    	try {    
    		LOGGER.info("Rest Server: joining Server - my work here is done...");
	        server.join();
    	} catch (Exception e) {
    		LOGGER.info("Rest Server: exception caught whill running: " + e.getLocalizedMessage() );
    		e.printStackTrace();
    	}
	        
    }
    
    private WebAppContext configureWebapp() {
        WebAppContext context = new WebAppContext();

        context.setContextPath("/gummy-rest");
    	try {
			LOGGER.info("extractediing war file from jar.....");
    		String extractedWarFile = JarFileExtractor.extractResource(WEBAPP_RESOURCES_LOCATION); 
			LOGGER.info("extracted war file from jar to " + extractedWarFile);
			context.setWar(extractedWarFile);
		} catch (Exception e) {
			LOGGER.severe("Exception extracting war file from jar: " + e.getLocalizedMessage());
		}
        // context.setParentLoaderPriority(true);
        return context;
    }	
    
    private long elapsed() {
    	return System.currentTimeMillis()-startTime;
    }

    ////////////////////////////////////////
    protected static Logger LOGGER = Logger.getLogger(LOGGER_NAME); 
}
