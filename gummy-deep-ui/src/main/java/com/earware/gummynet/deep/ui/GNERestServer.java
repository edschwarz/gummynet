	package com.earware.gummynet.deep.ui;

import java.net.URL;
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
    	new GNERestServer().startServer(args);
    }
    
	// -------------------------------------
    private GNERestServer() {}
	
	private long startTime = System.currentTimeMillis();
        	
    public void startServer(String[] args) {	
    	try {
    		Log.setLog(new StdErrLog());
    		LOGGER.info("Rest Server Startup at " + new Date());

    		LOGGER.info("Rest Server: creating Server...");
	        Server server = new Server(8080);
    		LOGGER.info("Rest Server: creating Server completed in " + elapsed() + " msec");
	        
    		LOGGER.info("Rest Server: configuring webapp gummy-rest...");
	        WebAppContext context = configureWebapp2();
            context.setAttribute("args", args);
    		LOGGER.info("Rest Server: configuring webapp gummy-rest completed in " + elapsed() + " msec");

            server.setHandler(context);
	        
    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
    		LOGGER.info("Rest Server: starting Server...");
	        server.start();
    		LOGGER.info("Rest Server: starting Server completed in  " + elapsed() + " msec");

	        server.dumpStdErr();
	        
    		LOGGER.info("Rest Server: joining Server - my work here is done...");
	        server.join();
    	} catch (Exception e) {
    		LOGGER.info("Rest Server: exception during UI startup: " + e.getLocalizedMessage() );
    		e.printStackTrace();
    	}
    }
    
    public GNERestServer(int port, Object gummyNetworkEvolver) {
    	startRestServer(port, gummyNetworkEvolver);
    }
    
    public WebAppContext configureWebapp() {
        WebAppContext context = new WebAppContext();
		context.setContextPath("/gummy-rest");
        String jettyHome = System.getenv("JETTY_HOME");
        context.setWar(jettyHome + "/webapps/gummy-rest.war");
        context.setLogger(new org.eclipse.jetty.util.log.JavaUtilLog(LOGGER_NAME));
        return context;
    }
    
    public WebAppContext configureWebapp2() {
        WebAppContext context = new WebAppContext();

        context.setContextPath("/gummy-rest");
/*
        URL webAppDir = Thread.currentThread().getContextClassLoader().getResource(WEBAPP_RESOURCES_LOCATION);
        if (webAppDir == null) {
            throw new RuntimeException(String.format("No %s directory was found into the JAR file", WEBAPP_RESOURCES_LOCATION));
        }
        String resourceBase;
        String resourceBase2;
        try {
            // resourceBase2 = webAppDir.toURI().toString();
            resourceBase = webAppDir.toString();
        } catch (Exception e) {
            throw new RuntimeException(String.format("could not convert URL \"" + webAppDir + "\" to URI: ", e));
        }
        */
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
    
    private void startRestServer(int port, Object gummyNetworkEvolver) {
		new Thread(new Runnable() {public void run() {
			try {
				
	    		Log.setLog(new StdErrLog());
	    		LOGGER.info("Rest Server Startup at " + new Date());

	    		LOGGER.info("Rest Server: creating Server");
		        Server server = new Server(8080);
	    		LOGGER.info("Rest Server: creating Server completed... " + elapsed() + " msec");
		        
	    		LOGGER.info("Rest Server: configuring webapp gummy-rest");
		        WebAppContext context = new WebAppContext();
	    		context.setContextPath("/gummy-rest");
		        String jettyHome = System.getenv("JETTY_HOME");
	            context.setWar(jettyHome + "/webapps/gummy-rest.war");
	            context.setAttribute("gummyNetworkEvolver", gummyNetworkEvolver);
	            context.setLogger(new org.eclipse.jetty.util.log.JavaUtilLog(LOGGER_NAME));            
	            server.setHandler(context);
	    		LOGGER.info("Rest Server: configuring webapp gummy-rest completed... " + elapsed() + " msec");
		        
	    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
	    		LOGGER.info("Rest Server: starting Server");
		        server.start();
	    		LOGGER.info("Rest Server: completed starting Server... " + elapsed() + " msec");

		        server.dumpStdErr();
		        
	    		LOGGER.info("Rest Server: joining Server - my work here is done...");
		        server.join();
		        
			} catch (Exception e) {
	    		LOGGER.info("Rest Server: exception during UI startup: " + e.getLocalizedMessage() );
					e.printStackTrace();
			}
		}}).start();
    }
    
    public long elapsed() {
    	return System.currentTimeMillis()-startTime;
    }

    ////////////////////////////////////////
    protected static Logger LOGGER = Logger.getLogger(LOGGER_NAME); 
}
