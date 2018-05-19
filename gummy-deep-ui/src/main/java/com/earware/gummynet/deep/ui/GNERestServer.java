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
	private static long startTime = System.currentTimeMillis();
    public static void main( String[] args ) 
    {
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
            context.setAttribute("args", args);
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
    }
    
    public GNERestServer(int port, Object gummyNetworkEvolver) {
    	startRestServer(port, gummyNetworkEvolver);
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
    
    public static long elapsed() {
    	return System.currentTimeMillis()-startTime;
    }

    ////////////////////////////////////////
    protected static Logger LOGGER = Logger.getLogger(LOGGER_NAME); 
}
