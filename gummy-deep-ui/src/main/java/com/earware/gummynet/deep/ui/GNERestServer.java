	package com.earware.gummynet.deep.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class GNERestServer {
	private static String LOGGER_NAME = GNERestServer.class.getName();
    public static void main( String[] args ) 
    {
    	try {
    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
	        Server server = new Server(8080);
	        
    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
	        WebAppContext context = new WebAppContext();

    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
    		context.setContextPath("/gummy-rest");

    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
	        String jettyHome = System.getenv("JETTY_HOME");
            context.setWar(jettyHome + "/webapps/gummy-rest.war");
            context.setAttribute("args", args);
            
            context.setLogger(new org.eclipse.jetty.util.log.JavaUtilLog(LOGGER_NAME));
            
    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
            server.setHandler(context);
	        
    		Logger.getLogger("org.eclipse.jetty").setLevel(Level.FINEST);
	        server.start();

	        server.dumpStdErr();
	        
	        server.join();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    /*
    */
    protected static Logger LOGGER = Logger.getLogger(LOGGER_NAME); 
}
