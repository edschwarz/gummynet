package com.earware.gummynet.deep;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import com.earware.gummynet.deep.GummyNetworkEvolver.Config;
import com.earware.gummynet.deep.ui.webapp.GNERestServer;


public class GNE {
    protected static Logger LOGGER = Logger.getLogger(GummyNetworkEvolver.class.getName()); 
    
    //**************************************************************************
    //**************************************************************************
    //**************************************************************************
    //**************************************************************************
    public static void main(String[] args) throws IOException {
        LOGGER.info("main: executed at: " + new Date());
        LOGGER.info("main: args: " + Arrays.toString(args));

        Config config = processCommandLine(args);
        if (config==null) return; // bad args
		
        try {
        	GummyNetworkEvolver evolver = new GummyNetworkEvolver();
        	GNERestServer restServer = new GNERestServer(8080,evolver);
        	GummyNetworkEvolver.Stats evolvedStats 
        				= evolver.evolve(config);
        		
        	if (evolvedStats!=null) {
        		LOGGER.info("main: winner: " + evolvedStats.toString());
        	} else {
        		LOGGER.info("main: no survivors");
        	}
        } catch (Exception e) {
    			e.printStackTrace();
        }
    }
    
    private static String usage="     -c GummyConfig json file path (default=new GummyConfig())\n" + 
    		"     -m mother dqn file path (default=random models will be generated/no actual progeny, if a directory is specified all dqn files in the directory will be benchmarked into the pool)\n" + 
    		"     -p progeny to create (default=10)\n" + 
    		"     -l learning hands per progeny (default=30)\n" + 
    		"     -s stochastic hands per learning (default=10, must be < -l)\n" + 
    		"     -f filter (for complete losers) hands per progeny (default=40)\n" + 
    		"     -w minimum wins needed to pass filter (default=0)\n" + 
    		"     -t minimum fitness needed to pass filter (default=0)\n" + 
    		"     -v validation hands per progeny (default=400)\n" + 
    		"     -b bot-based training (default=true, if false each progeny will train against itself)\n" + 
    		"     -d directory to create-or-use (default=\"./gne\"+hashcode(ctor datetime))\n" +
    		""
    		;
    
    public static void printUsage() {LOGGER.info(usage);}
    //**************************************************************************
    
    public static Config processCommandLine(String[] args) {
        Config config=new Config();
		
		for (int i=0; i<args.length; i++) {
			String arg=args[i];
			if (arg.startsWith("-") && arg.length()>1) {
				String flag=arg.substring(1);
				if        (flag.equals("c") && args.length>i+1) {
					config.gummyConfigFilePath=args[++i];
				} else if (flag.equals("m") && args.length>i+1) {
					config.motherDqnFilePath=args[++i];
				} else if (flag.equals("p") && args.length>i+1) {
					config.progenyPerGeneration=Integer.parseInt(args[++i]);
				} else if (flag.equals("l") && args.length>i+1) {
					config.learningHandsPerProgeny=Integer.parseInt(args[++i]);
				} else if (flag.equals("s") && args.length>i+1) {
					config.stochasticHandsPerLearning=Integer.parseInt(args[++i]);
				} else if (flag.equals("d") && args.length>i+1) {
					config.targetDirectoryForEvolution=args[++i];
				} else if (flag.equals("b") && args.length>i+1) {
					config.useBot=Boolean.parseBoolean(args[++i]);
				} else if (flag.equals("v") && args.length>i+1) {
					config.validationHandsPerProgeny=Integer.parseInt(args[++i]);
				} else if (flag.equals("f") && args.length>i+1) {
					config.filterHandsPerProgeny=Integer.parseInt(args[++i]);
				} else if (flag.equals("w") && args.length>i+1) {
					config.filterMinimumWins=Integer.parseInt(args[++i]);
				} else if (flag.equals("t") && args.length>i+1) {
					config.filterMinimumFitness=Double.parseDouble(args[++i]);
				} else {
					LOGGER.warning("unknown flag: " + arg); 
					printUsage();
				}
			} else {
				LOGGER.warning("expected flag, got: " + arg); 
				printUsage();
				return null;
			}
		}
		return config;
    }
    
    
    //**************************************************************************
    //**************************************************************************
    //**************************************************************************

}
