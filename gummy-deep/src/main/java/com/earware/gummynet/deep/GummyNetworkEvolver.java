package com.earware.gummynet.deep;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GummyNetworkEvolver {
	public final static String MODELS_DIR = "models";
	public final static String LOGS_DIR = "logs";
	public final static String LOCAL_GUMMY_CONFIG_FILE_NAME = "mother.cfg";
	public final static String LOCAL_GUMMY_CONFIG_PATH = LOCAL_GUMMY_CONFIG_FILE_NAME;
	public final static String LOCAL_MOTHER_DQN_FILE_NAME = "mother.dqn";
	public final static String LOCAL_MOTHER_DQN_PATH = MODELS_DIR + "/" + LOCAL_MOTHER_DQN_FILE_NAME;
	public final static String LOCAL_EVOLVED_DQN_FILE_NAME = "evolved.dqn";
	public final static String LOCAL_EVOLVED_DQN_PATH = MODELS_DIR + "/" + LOCAL_MOTHER_DQN_FILE_NAME;
	public final static String LOCAL_LOG_FILE_NAME = "GummyNetworkEvolver.log";
	public final static String LOCAL_LOG_PATH = LOGS_DIR + "/" + LOCAL_LOG_FILE_NAME;
	
    //**************************************************************************
    //**************************************************************************
    public static class Config {
		String gummyConfigFilePath=null;
		String motherDqnFilePath=null;
		String evolvedDqnFilePath=null;
		int progenyPerGeneration=10;
		int learningHandsPerProgeny=30;
		int stochasticHandsPerLearning=10;
		int filterHandsPerProgeny=40;
		int filterMinimumWins=0;
		double filterMinimumFitness=0;
		int validationHandsPerProgeny=400;
		boolean useBot=true;
		String targetDirectoryForEvolution="./gne"+Integer.toHexString(new Long(System.currentTimeMillis()).hashCode());
		
		public Config() {}
		public Config(String targetDirectory) {this.targetDirectoryForEvolution=targetDirectory;}
		
		@Override
		public String toString() {
			return Config.toString(this);
		}
		public static String toString(Config config) {
			return ""
					+ "   motherDqnFilePath=" + config.motherDqnFilePath 
					+ "   evolvedDqnFilePath=" + config.evolvedDqnFilePath 
					+ "   progenyPerGeneration=" + config.progenyPerGeneration
					+ "   learningHandsPerProgeny=" + config.learningHandsPerProgeny
					+ "   stochasticHandsPerLearning=" + config.stochasticHandsPerLearning
					+ "   gummyConfigFilePath=" + config.gummyConfigFilePath
    					+ "   useBot=" + config.useBot
    					+ "   filterHandsPerProgeny=" + config.filterHandsPerProgeny
    					+ "   filterMinimumWins=" + config.filterMinimumWins
    					+ "   filterMinimumFitness=" + config.filterMinimumFitness
    					+ "   validationHandsPerProgeny=" + config.validationHandsPerProgeny
    					+ "   targetDirectoryForEvolution=" + config.targetDirectoryForEvolution
    				;
		}
		@Override
		public Config clone() {
			return Config.copy(this);
		}
		public static Config copy(Config original) {
			Config rez = new Config();
			rez.gummyConfigFilePath = original.gummyConfigFilePath;
			rez.learningHandsPerProgeny = original.learningHandsPerProgeny;
			rez.motherDqnFilePath = original.motherDqnFilePath;
			rez.evolvedDqnFilePath = original.evolvedDqnFilePath;
			rez.progenyPerGeneration = original.progenyPerGeneration;
			rez.stochasticHandsPerLearning = original.stochasticHandsPerLearning;
			rez.targetDirectoryForEvolution = original.targetDirectoryForEvolution;
			rez.useBot = original.useBot;
			rez.filterHandsPerProgeny = original.filterHandsPerProgeny;
			rez.filterMinimumWins = original.filterMinimumWins;
			rez.filterMinimumFitness = original.filterMinimumFitness;
			rez.validationHandsPerProgeny = original.validationHandsPerProgeny;
			return rez;
		}
    }
    
    //**************************************************************************
    protected List<GNEParentPool.GneParentStats> evolve(GummyNetworkEvolver.Config config) throws IOException {
		LOGGER.info("OKOKOKOKOKOKOKOKOKKKK evolving: " + config.toString());
    		return evolveDirectory(config);
    }
    
    protected List<GNEParentPool.GneParentStats> evolveDirectory(GummyNetworkEvolver.Config config) throws IOException {
		Config localConfig = Config.copy(config);
		
   		String targetDirectoryForEvolution = config.targetDirectoryForEvolution;
    		if (targetDirectoryForEvolution==null) {
    			targetDirectoryForEvolution = ".";
    			localConfig.targetDirectoryForEvolution = targetDirectoryForEvolution;
    		}
    		
    		// validate a few things before writing files
    		if (config.gummyConfigFilePath!=null && config.gummyConfigFilePath.length()>0) {
    			// will throw exception if not valid
    			GummyConfig.fromFile(config.gummyConfigFilePath);
    		}
    		if (config.motherDqnFilePath!=null && config.motherDqnFilePath.length()>0) {
    			// TODO - currently loadModel is private in gummyRunner, maybe GummyDQN? Factory issues?
    			File motherFile = new File(config.motherDqnFilePath);
    			if (!motherFile.exists() || !motherFile.canRead() || !motherFile.isFile()) {
    				throw new IllegalArgumentException("motherDqnFilePath \"" + config.motherDqnFilePath + "\": cannot find/read/use this file");
    			}
    		}
    		
    		File targetDir=new File(targetDirectoryForEvolution);
		File modelsDir = new File(targetDir,MODELS_DIR);
		File logsDir = new File(targetDir,LOGS_DIR);
    		if (!modelsDir.exists()) {
    			modelsDir.mkdirs();
    		}
    		if (!logsDir.exists()) {
    			logsDir.mkdirs();
    		}
    		
    		fixupLogging(logsDir);
    		LOGGER.info("evolution begins: " + config.toString());
    		
		localConfig.gummyConfigFilePath = targetDir.getPath() + "/" + LOCAL_GUMMY_CONFIG_PATH;
    		if (config.gummyConfigFilePath!=null && config.gummyConfigFilePath.length()>0) {
    			fileCopy(new File(config.gummyConfigFilePath), new File(localConfig.gummyConfigFilePath));
    		} else {
    			// saving snapshot of default config
    			new GummyConfig().save(localConfig.gummyConfigFilePath);
    		}
    		if (config.motherDqnFilePath!=null && config.motherDqnFilePath.length()>0) {
    			localConfig.motherDqnFilePath = targetDir.getPath() + "/" + LOCAL_MOTHER_DQN_PATH;
    			fileCopy(new File(config.motherDqnFilePath), new File(localConfig.motherDqnFilePath));
    		}
    		
    		List<GNEParentPool.GneParentStats> gneStats = evolveLocal(localConfig);
    		
    		LOGGER.info("evolution completed: " + gneStats.toString());
    		if (localConfig.evolvedDqnFilePath==null || localConfig.evolvedDqnFilePath.length()==0) {
			localConfig.evolvedDqnFilePath = targetDir.getPath() + "/" + LOCAL_EVOLVED_DQN_PATH;
    		}
    		LOGGER.info("copying " + gneStats.get(0).dqnPath + " to " + localConfig.evolvedDqnFilePath);
		fileCopy(new File(gneStats.get(0).dqnPath), 
					new File(localConfig.evolvedDqnFilePath));
		
    		return gneStats;
    }
    
    protected File fileCopy(File source, File target) throws IOException {
    		return Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING).toFile();
    }

    protected File createTempDir() throws IOException {
    		return Files.createTempDirectory(
    				"GNEtmp" + System.currentTimeMillis()).toFile();

    }

    protected List<GNEParentPool.GneParentStats> evolveLocal(GummyNetworkEvolver.Config localConfig) throws IOException {
    	
		LOGGER.info("evolving with local GNE config: " + localConfig.toString());
    	
		// fix up learning config
		GummyConfig templateConfig = GummyConfig.fromFile(localConfig.gummyConfigFilePath);
		templateConfig.setApproxHandsToPlay(localConfig.learningHandsPerProgeny);
		templateConfig.setGreedyAnnealHands(localConfig.stochasticHandsPerLearning);
		LOGGER.info("evolving with local Gummy config: " + templateConfig.toString());

		// set up temp directory
		GNEParentPool pool = new GNEParentPool(new File(localConfig.targetDirectoryForEvolution + "/" + MODELS_DIR));

		// set up temp directory
		File tmpDir = createTempDir();
		tmpDir.deleteOnExit();
		
		// baseline input model, if any
		if (localConfig.motherDqnFilePath!=null) {
			String mother = localConfig.motherDqnFilePath;
			LOGGER.info("BASELINE - model " + mother + ": baselining begins");
			GummyDeepPlayGin.Stats motherStats = GummyDeepPlayGin.play(mother, "bot", localConfig.validationHandsPerProgeny, 0);
			LOGGER.info("BASELINE - model " + mother + ": " + motherStats.winCounts[0] + " model wins -- " + motherStats.toString());
			pool.addParent(mother, motherStats);
		}
				
		for (int progeny=1; progeny<=localConfig.progenyPerGeneration; progeny++) {
			
			// spawn progeny model        		
    			String progenyPath = null;
    			String parentDqnPath = pool.getParentFromPool();
        		if (parentDqnPath != null) {
        			File parentDqnFile = new File(parentDqnPath);
        			File f = new File(tmpDir, parentDqnFile.getName() + "GummyNetworkEvolver.dqnCopy." + progeny);
        			fileCopy(parentDqnFile, f);
        			progenyPath = f.getPath();
        			LOGGER.info("SPAWN - model " + progeny + ": copied from source " + parentDqnPath);
        		} else {
        			LOGGER.info("SPAWN - model " + progeny + ": no source model, will create a new model");
        		}

			// train
    			LOGGER.info("TRAIN - model " + progeny + ": training begins");
    			long start = System.currentTimeMillis();
        		GummyRunner.GummyRunnerStats runnerStats = train(progenyPath, 
        				tmpDir, 
        				templateConfig, 
        				localConfig.targetDirectoryForEvolution);
    			LOGGER.info("TRAIN - model " + progeny + ": training completed, " + (System.currentTimeMillis()-start) + "ms");
    			progenyPath = runnerStats.player1TargetDqnPath;
        		
			// filter
    			LOGGER.info("FILTER - model " + progeny + ": filtering begins");
        		GummyDeepPlayGin.Stats filterStats = GummyDeepPlayGin.play(progenyPath, "bot", localConfig.filterHandsPerProgeny, 0);
        		if (shouldFilter(localConfig, filterStats)) {
        			// fell in the hole. Forget it.
        			LOGGER.info("FILTER - FAIL for model " + progeny + ": " + filterStats.winCounts[0] + " model wins -- " + filterStats.toString());
        			continue;
        		} else {
        			LOGGER.info("FILTER - model " + progeny + ": " + filterStats.winCounts[0] + " model wins -- " + filterStats.toString());
        		}
        		
			// validate
    			LOGGER.info("VALIDATE - model " + progeny + ": validation begins");
        		GummyDeepPlayGin.Stats validateStats = GummyDeepPlayGin.play(progenyPath, "bot", localConfig.validationHandsPerProgeny, 0);
        		LOGGER.info("VALIDATE - model " + progeny + ": " + validateStats.winCounts[0] + " model wins -- " + validateStats.toString());
        		
        		// add to pool
        		pool.addProgeny(progenyPath, validateStats, parentDqnPath);
        		        		
		}	
		
		return pool.getStatsList();
    }
    
    private GummyRunner.GummyRunnerStats train( String dqnPath, 
    											   File tmpDir, 
    											   GummyConfig gummyConfig,
    											   String newModelCreatePath)  throws IOException {
    	
		GummyRunnerConfig runnerConfig = getGenerationRunnerConfig(
				gummyConfig.getApproxHandsToPlay(), 
				gummyConfig, 
				tmpDir, 
				"CONFIG",
				false,
				true);                       
		runnerConfig.player1DqnPath = dqnPath;
		runnerConfig.player2DqnPath = "bot";
		runnerConfig.player2Name = "bot";
		
		GummyRunner runner = new GummyRunner();
		runner.defaultModelSavePath = newModelCreatePath + "/" + GummyNetworkEvolver.MODELS_DIR;
		GummyRunner.GummyRunnerStats runnerStats = runner.gummyRunner(runnerConfig);
		return runnerStats;
    }
    
    private boolean shouldFilter(GummyNetworkEvolver.Config localConfig,
    			GummyDeepPlayGin.Stats filterStats) {
    		if (filterStats.drawDeckCount == 0 || filterStats.drawPileCount==0) {
    			return true;
    		}
    		if (filterStats.winCounts[0] < localConfig.filterMinimumWins) {
    			return true;
    		}
    		if (filterStats.p1Fitness() < localConfig.filterMinimumFitness) {
    			return true;
    		}
    		return false;
    	}
    
    protected GummyRunnerConfig getGenerationRunnerConfig(int handsPerRun, 
    										GummyConfig gummyConfig, 
    										File tmpDir, String configCopyPrefix,
    										boolean useSingleDqn, boolean useBot) 
    						throws IOException {
		GummyRunnerConfig runnerConfig = new GummyRunnerConfig();
		runnerConfig.approxNumberOfHandsToPlay = handsPerRun;
		runnerConfig.useSingleDqn = useSingleDqn;
		String gummyConfigPath = configCopyPrefix+".GummyNetworkEvolver.gummyConfigCopy";
		File cf=new File(tmpDir, gummyConfigPath);
		gummyConfig.save(cf.getPath());
        runnerConfig.player1ConfigPath = cf.getPath();
        runnerConfig.player1IsTraining = true;
        runnerConfig.player1Name = "trainee";
        if (useBot) {
        		runnerConfig.player2ConfigPath = cf.getPath();
        		runnerConfig.player2IsTraining = false;
            runnerConfig.player2Name = "bot";
        } else {
        		runnerConfig.player2ConfigPath = cf.getPath();
        		runnerConfig.player2IsTraining = true;
        }
        
        return runnerConfig;
    }
    

    private static int turnEntry(int turns) {
    		return 200-turns;
    }
    
    private static double fitnessEntry(double[] histogram) {
    		return GummySmartGinStrategy.evaluateHistogramFit(histogram);
    }
    
    private static final double IDEAL_DRAW_RATIO = 0.1; // most of the time you should draw from the deck - just a guess ejs 3/7/2018 
    private static double pileRatioEntry(GummyDeepPlayGin.Stats playStats) {
		double ratio = ((double)playStats.drawPileCount)/((double)playStats.turns);
		return 1.0-Math.abs(ratio-IDEAL_DRAW_RATIO);
    }
    
    private static final double IDEAL_DISCARD_RATIO = 0.15; // ? most cards in your hand should NOT be discarded - just a guess ejs 3/7/2018
    private static double discardRatioEntry(GummyDeepPlayGin.Stats playStats) {
		double ratio = ((double)playStats.voteDiscardCount)/((double)(playStats.voteDiscardCount+playStats.voteKeepCount));
		return 1.0-Math.abs(ratio-IDEAL_DISCARD_RATIO);
    }
    
    protected static Logger LOGGER = Logger.getLogger(GummyNetworkEvolver.class.getName()); 
    private static void configLogger(Logger logger, String logPath) {try {Handler handler=new FileHandler(logPath); handler.setFormatter(new SimpleFormatter()); logger.addHandler(handler);} catch (Exception e) {e.printStackTrace();}}
	// static {configLogger(LOGGER, LOCAL_LOG_PATH);}
    private static void fixupLogging(File logsDir) {
		String loggerName = LOGGER.getName();
		LOGGER=Logger.getLogger(loggerName+"_Evolver");
		GummyNetworkEvolver.configLogger(LOGGER, logsDir.getPath() + "/" + GummyNetworkEvolver.LOCAL_LOG_FILE_NAME);
		
		GummyRunner.LOGGER=Logger.getLogger(loggerName+"_Runner");
		GummyNetworkEvolver.configLogger(GummyRunner.LOGGER, logsDir.getPath() + "/GummyRunner.log");
	
		GummyDeepGinStrategy.LOGGER = Logger.getLogger(loggerName+"_others");
		GummyDeepPlayGin.LOGGER=Logger.getLogger(loggerName+"_Player");
		GummyNetworkEvolver.configLogger(GummyDeepPlayGin.LOGGER, logsDir.getPath() + "/GummyDeepPlayGin.log");
	
		Logger lotsaLogger = Logger.getLogger(GummyMDP.class.getSimpleName());
		GummyDeepUI.LOGGER = Logger.getLogger(loggerName+"_others"); 
		GummySimulator.LOGGER = Logger.getLogger(loggerName+"_others");
		GummyMDP.LOGGER = Logger.getLogger(loggerName+"_others");
		// StatsGummyMDP.LOGGER = Logger.getLogger(loggerName+"_others");
		GummyDeepGinStrategy.LOGGER = Logger.getLogger(loggerName+"_others");
		
		GummyNetworkEvolver.configLogger(Logger.getLogger(loggerName+"_others"),logsDir.getPath() + "/GummyDeep.log");
		if (lotsaLogger.getHandlers().length>0) {
			lotsaLogger.removeHandler(lotsaLogger.getHandlers()[lotsaLogger.getHandlers().length-1]);
		}
		GummyNetworkEvolver.configLogger(lotsaLogger,logsDir.getPath() + "/GummyDeep.log");
	}
    
    //**************************************************************************
    //**************************************************************************
    //**************************************************************************
    //**************************************************************************
    public static void main(String[] args) throws IOException {
        LOGGER.info("main: executed at: " + new Date());
        LOGGER.info("main: args: " + Arrays.toString(args));

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
				return;
			}
		}
		
        try {
        		List<GNEParentPool.GneParentStats> evolvedStats 
        				= new GummyNetworkEvolver().evolveDirectory(config);
        		
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
    		"     -m mother dqn file path (default=random models will be used for the first generation progeny)\n" + 
    		"     -p progeny per generation [formerly \"runners\"] (default=10)\n" + 
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
    //**************************************************************************
    //**************************************************************************
    //**************************************************************************
}
