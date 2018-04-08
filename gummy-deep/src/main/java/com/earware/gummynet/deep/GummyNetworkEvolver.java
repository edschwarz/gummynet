package com.earware.gummynet.deep;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.output.NullOutputStream;

import com.earware.gummynet.gin.PlayGin;
import com.google.common.io.Files;

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
		int generations=1;
		int progenyPerGeneration=10;
		int learningHandsPerProgeny=30;
		int stochasticHandsPerLearning=10;
		int filterHandsPerProgeny=40;
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
					+ "   generations=" + config.generations
					+ "   progenyPerGeneration=" + config.progenyPerGeneration
					+ "   learningHandsPerProgeny=" + config.learningHandsPerProgeny
					+ "   stochasticHandsPerLearning=" + config.stochasticHandsPerLearning
					+ "   gummyConfigFilePath=" + config.gummyConfigFilePath
    					+ "   useBot=" + config.useBot
    					+ "   filterHandsPerProgeny=" + config.filterHandsPerProgeny
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
			rez.generations = original.generations;
			rez.gummyConfigFilePath = original.gummyConfigFilePath;
			rez.learningHandsPerProgeny = original.learningHandsPerProgeny;
			rez.motherDqnFilePath = original.motherDqnFilePath;
			rez.evolvedDqnFilePath = original.evolvedDqnFilePath;
			rez.progenyPerGeneration = original.progenyPerGeneration;
			rez.stochasticHandsPerLearning = original.stochasticHandsPerLearning;
			rez.targetDirectoryForEvolution = original.targetDirectoryForEvolution;
			rez.useBot = original.useBot;
			rez.filterHandsPerProgeny = original.filterHandsPerProgeny;
			rez.validationHandsPerProgeny = original.validationHandsPerProgeny;
			return rez;
		}
    }
    
    //**************************************************************************
    protected ScoredStats evolve(GummyNetworkEvolver.Config config) throws IOException {
		LOGGER.info("OKOKOKOKOKOKOKOKOKKKK evolving: " + config.toString());
    		return evolveDirectory(config);
    }
    
    protected ScoredStats evolveDirectory(GummyNetworkEvolver.Config config) throws IOException {
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
    			Files.copy(new File(config.gummyConfigFilePath), new File(localConfig.gummyConfigFilePath));
    		} else {
    			// saving snapshot of default config
    			new GummyConfig().save(localConfig.gummyConfigFilePath);
    		}
    		if (config.motherDqnFilePath!=null && config.motherDqnFilePath.length()>0) {
    			localConfig.motherDqnFilePath = targetDir.getPath() + "/" + LOCAL_MOTHER_DQN_PATH;
    			Files.copy(new File(config.motherDqnFilePath), new File(localConfig.motherDqnFilePath));
    		}
    		
    		ScoredStats winningStats = evolveLocal(localConfig);
    		
    		LOGGER.info("evolution completed: " + winningStats.toString());
    		if (localConfig.evolvedDqnFilePath==null || localConfig.evolvedDqnFilePath.length()==0) {
			localConfig.evolvedDqnFilePath = targetDir.getPath() + "/" + LOCAL_EVOLVED_DQN_PATH;
    		}
    		LOGGER.info("copying " + winningStats.stats.player1TargetDqnPath + " to " + localConfig.evolvedDqnFilePath);
		Files.copy(new File(winningStats.stats.player1TargetDqnPath), 
					new File(localConfig.evolvedDqnFilePath));
		
    		return winningStats;
    }
    
    protected ScoredStats evolveLocal(GummyNetworkEvolver.Config localConfig) throws IOException {
    	
		LOGGER.info("evolving with local GNE config: " + localConfig.toString());
    	
		String templateDqnFilePath = localConfig.motherDqnFilePath;
		GummyConfig templateConfig = GummyConfig.fromFile(localConfig.gummyConfigFilePath);
		templateConfig.setApproxHandsToPlay(localConfig.learningHandsPerProgeny);
		templateConfig.setGreedyAnnealHands(localConfig.stochasticHandsPerLearning);
		LOGGER.info("evolving with local Gummy config: " + templateConfig.toString());
		ScoredStats templateScoredStats = new ScoredStats();
		
		// set up
		File tmpDir = Files.createTempDir();
		tmpDir.deleteOnExit();
		File templateDqnFile = null;
		GummyDeepPlayGin.Stats templateStats = null;
		if (templateDqnFilePath!=null) {
			templateDqnFile = new File(templateDqnFilePath);
			LOGGER.info("BASELINE - model " + templateDqnFilePath + ": baselining begins");
			templateStats = GummyDeepPlayGin.play(templateDqnFilePath, "bot", localConfig.validationHandsPerProgeny, 0);
			templateScoredStats.playStats = templateStats;
			LOGGER.info("BASELINE - model " + templateDqnFilePath + ": " + templateStats.winCounts[0] + " model wins -- " + templateStats.toString());
		}
		
		int templateReplacements = 0;
		for (int progeny=1; progeny<=localConfig.progenyPerGeneration; progeny++) {
			
			// create model        		
    			String runnerPath = null;
        		if (templateDqnFile != null) {
        			File f = new File(tmpDir, templateDqnFile.getName() + "GummyNetworkEvolver.dqnCopy." + progeny);
        			Files.copy(templateDqnFile, f);
        			runnerPath = f.getPath();
        		}

			// train
    			LOGGER.info("TRAIN - model " + progeny + ": training begins");
    			long start = System.currentTimeMillis();
        		GummyRunner.GummyRunnerStats runnerStats = train(runnerPath, 
        				tmpDir, 
        				templateConfig, 
        				localConfig.targetDirectoryForEvolution);
    			LOGGER.info("TRAIN - model " + progeny + ": training completed, " + (System.currentTimeMillis()-start) + "ms");
    			runnerPath = runnerStats.player1TargetDqnPath;
        		
			// filter
    			LOGGER.info("FILTER - model " + progeny + ": filtering begins");
        		GummyDeepPlayGin.Stats filterStats = GummyDeepPlayGin.play(runnerPath, "bot", localConfig.filterHandsPerProgeny, 0);
        		if (filterStats.drawDeckCount == 0 || filterStats.drawPileCount==0) {
        			// fell in the hole. Forget it.
        			LOGGER.info("FILTER - FAIL for model " + progeny + ": " + filterStats.winCounts[0] + " model wins -- " + filterStats.toString());
        			continue;
        		} else {
        			LOGGER.info("FILTER - model " + progeny + ": " + filterStats.winCounts[0] + " model wins -- " + filterStats.toString());
        		}
        		
			// validate
    			LOGGER.info("VALIDATE - model " + progeny + ": validation begins");
        		GummyDeepPlayGin.Stats validateStats = GummyDeepPlayGin.play(runnerPath, "bot", localConfig.validationHandsPerProgeny, 0);
        		LOGGER.info("VALIDATE - model " + progeny + ": " + validateStats.winCounts[0] + " model wins -- " + validateStats.toString());
        		
			// replace prototype?
        		if (shouldReplace(validateStats, templateStats)) {
        			templateReplacements++;
        			String newTemplateFilePath = localConfig.targetDirectoryForEvolution + "/" + MODELS_DIR + "/"
        								+ templateDqnFile.getName() + ".step" + templateReplacements;
	    			LOGGER.info("EVOLVE - model at " + newTemplateFilePath + " will replace " + templateDqnFilePath + " as template");
	    			LOGGER.info("EVOLVE - copying " + runnerPath + " to " + newTemplateFilePath);
	    	        Files.copy(new File(runnerPath), 
	        				new File(newTemplateFilePath));

	    	        templateDqnFilePath = newTemplateFilePath;
	    	        templateStats = validateStats;
        			templateDqnFile = new File(templateDqnFilePath);
        			templateScoredStats.playStats = templateStats;
        			templateScoredStats.stats = runnerStats;
        			templateScoredStats.stats.player1TargetDqnPath = templateDqnFilePath;
        		}	
		}	
		
		return templateScoredStats;
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
    
    private boolean shouldReplace(GummyDeepPlayGin.Stats candidateStats, 
    								GummyDeepPlayGin.Stats templateStats) {
	    	if (templateStats==null) {
	    		// generating new models - no template
	    		return false;
	    	}
	    	if (candidateStats.winCounts[0] > templateStats.winCounts[0]) {
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
    

    private class ScoredStats implements Comparable<ScoredStats> {
    		GummyRunner.GummyRunnerStats stats = null;
    		GummyDeepPlayGin.Stats playStats = null;
    		double score = 0;
		@Override
		public int compareTo(ScoredStats o) {return Double.compare(((ScoredStats)o).score, this.score);}
		@Override
		public String toString() {
			String rez = "score=" + String.format("%3.3f", score);
			if (playStats!=null) rez += "\n" + playStats.toString();
			if (stats!=null) rez += "\n" + stats.toString();
			return rez; 
		}
	}
      
    private static int turnEntry(int turns) {
    		return 200-turns;
    }
    
    private static double fitnessEntry(double[] histogram) {
    		return GummySmartGinStrategy.evaluateHistogramFit(histogram);
    }
    
    private static final double IDEAL_DRAW_RATIO = 0.1; // most of the time you should draw from the deck - just a guess ejs 3/7/2018 
    private static double pileRatioEntry(GummyDeepPlayGin.Stats playStats) {
		double ratio = ((double)playStats.drawPileCount)/((double)playStats.turns());
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
				if (flag.equals("c") && args.length>i+1) {
					config.gummyConfigFilePath=args[++i];
				} else if (flag.equals("m") && args.length>i+1) {
					config.motherDqnFilePath=args[++i];
				} else if (flag.equals("g") && args.length>i+1) {
					config.generations=Integer.parseInt(args[++i]);
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
        		ScoredStats evolvedStats = new GummyNetworkEvolver().evolveDirectory(config);
        		
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
    		"     -g generations (default=1)\n" + 
    		"     -p progeny per generation [formerly \"runners\"] (default=10)\n" + 
    		"     -l learning hands per progeny (default=30)\n" + 
    		"     -s stochastic hands per learning (default=10, must be < -l)\n" + 
    		"     -f filter (for complete losers) hands per progeny (default=40)\n" + 
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
