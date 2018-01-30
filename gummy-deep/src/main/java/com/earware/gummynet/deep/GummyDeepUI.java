package com.earware.gummynet.deep;

import org.deeplearning4j.ui.api.UIServer;

import java.util.logging.Logger;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.nn.api.Model;

public class GummyDeepUI {
	
	static UIServer uiServer;
	static StatsStorage statsStorage;
	
	public static void startUI(Model net) {
		try {
			//Initialize the user interface backend
			GummyDeepUI.uiServer = UIServer.getInstance();
	
		    //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
			GummyDeepUI.statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later
	
		    //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
			GummyDeepUI.uiServer.attach(statsStorage);
	
		    //Then add the StatsListener to collect this information from the network, as it trains
		    net.setListeners(new StatsListener(GummyDeepUI.statsStorage));
		} catch (Exception e) {
			LOGGER.warning("exception starting UI, running blind: " + e.getMessage());
		}
	}
	
	public static void stopUI() {
		GummyDeepUI.uiServer.detach(statsStorage);
	}
    
    public static Logger LOGGER = Logger.getLogger(GummyMDP.class.getPackage().getName()); 
}
