package com.earware.gummynet.deep;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GNELogging {
    protected static Logger LOGGER = Logger.getLogger(GummyNetworkEvolver.class.getName()); 
    private static void configLogger(Logger logger, String logPath) {try {Handler handler=new FileHandler(logPath); handler.setFormatter(new SimpleFormatter()); logger.addHandler(handler);} catch (Exception e) {e.printStackTrace();}}
	// static {configLogger(LOGGER, LOCAL_LOG_PATH);}
    public static void fixupLogging(File logsDir) {
		String loggerName = LOGGER.getName();
		LOGGER=Logger.getLogger(loggerName+"_Evolver");
		GNELogging.configLogger(LOGGER, logsDir.getPath() + "/" + GummyNetworkEvolver.LOCAL_LOG_FILE_NAME);
		
		GummyRunner.LOGGER=Logger.getLogger(loggerName+"_Runner");
		GNELogging.configLogger(GummyRunner.LOGGER, logsDir.getPath() + "/GummyRunner.log");
	
		GummyDeepGinStrategy.LOGGER = Logger.getLogger(loggerName+"_others");
		GummyDeepPlayGin.LOGGER=Logger.getLogger(loggerName+"_Player");
		GNELogging.configLogger(GummyDeepPlayGin.LOGGER, logsDir.getPath() + "/GummyDeepPlayGin.log");
	
		Logger lotsaLogger = Logger.getLogger(GummyMDP.class.getSimpleName());
		GummyDeepUI.LOGGER = Logger.getLogger(loggerName+"_others"); 
		GummySimulator.LOGGER = Logger.getLogger(loggerName+"_others");
		GummyMDP.LOGGER = Logger.getLogger(loggerName+"_others");
		// StatsGummyMDP.LOGGER = Logger.getLogger(loggerName+"_others");
		GummyDeepGinStrategy.LOGGER = Logger.getLogger(loggerName+"_others");
		
		GNELogging.configLogger(Logger.getLogger(loggerName+"_others"),logsDir.getPath() + "/GummyDeep.log");
		if (lotsaLogger.getHandlers().length>0) {
			lotsaLogger.removeHandler(lotsaLogger.getHandlers()[lotsaLogger.getHandlers().length-1]);
		}
		GNELogging.configLogger(lotsaLogger,logsDir.getPath() + "/GummyDeep.log");
	}    
}
