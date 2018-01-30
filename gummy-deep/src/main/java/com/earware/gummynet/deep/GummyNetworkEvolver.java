package com.earware.gummynet.deep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.google.common.io.Files;

public class GummyNetworkEvolver {
	public final static String MODELS_DIR = "models";
	public final static String LOGS_DIR = "logs";
	public final static String LOCAL_GUMMY_CONFIG_FILE_NAME = "mother.cfg";
	public final static String LOCAL_GUMMY_CONFIG_PATH = LOCAL_GUMMY_CONFIG_FILE_NAME;
	public final static String LOCAL_MOTHER_DQN_FILE_NAME = "mother.dqn";
	public final static String LOCAL_MOTHER_DQN_PATH = MODELS_DIR + "/" + LOCAL_MOTHER_DQN_FILE_NAME;
	public final static String LOCAL_LOG_FILE_NAME = "GummyNetworkEvolver.log";
	public final static String LOCAL_LOG_PATH = LOGS_DIR + "/" + LOCAL_LOG_FILE_NAME;
	
    //**************************************************************************
    //**************************************************************************
    public static class Config {
		String gummyConfigFilePath=null;
		String motherDqnFilePath=null;
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
    		LOGGER.info("evolving: " + config.toString());
    		
		localConfig.gummyConfigFilePath = targetDir.getPath() + "/" + LOCAL_GUMMY_CONFIG_PATH;
    		if (config.gummyConfigFilePath!=null && config.gummyConfigFilePath.length()>0) {
    			Files.copy(new File(config.gummyConfigFilePath), new File(localConfig.gummyConfigFilePath));
    		} else {
    			// otherwise it won't be saved
    			new GummyConfig().save(localConfig.gummyConfigFilePath);
    		}
    		if (config.motherDqnFilePath!=null && config.motherDqnFilePath.length()>0) {
    			localConfig.motherDqnFilePath = targetDir.getPath() + "/" + LOCAL_MOTHER_DQN_PATH;
    			Files.copy(new File(config.motherDqnFilePath), new File(localConfig.motherDqnFilePath));
    		}
    		
    		return evolveLocal(localConfig);
    }
    
    protected ScoredStats evolveLocal(GummyNetworkEvolver.Config localConfig) throws IOException {
    	
		LOGGER.info("evolving with local config: " + localConfig.toString());
    	
		String generationDqnFilePath = localConfig.motherDqnFilePath;
		GummyConfig generationConfig = GummyConfig.fromFile(localConfig.gummyConfigFilePath);
		ScoredStats winningStats = null;
    		for (int i=0; i<localConfig.generations; i++) {
    			
    			LOGGER.info("starting generation " + (i+1) + " using " + generationDqnFilePath);
    	        List<ScoredStats> scoreCard = runGeneration(localConfig,
						generationDqnFilePath,
						generationConfig);
    	        
    	        if (scoreCard.size()==0) {
	    			LOGGER.info("completed generation " + (i+1) + " There were no survivors. It happens.");
	    			break;
    	        } else {
	    			LOGGER.info("completed generation " + (i+1) + " There were " + scoreCard.size() + " viable progeny.");
    	        		for (int j=0; j<scoreCard.size(); j++) {
    	        			ScoredStats survivorStats = scoreCard.get(j);
    	        			String tmpDqnPath = survivorStats.stats.player1TargetDqnPath;
    	        			String saveDqnPath =  localConfig.targetDirectoryForEvolution +"/" + MODELS_DIR + "/evolve.generation." + (i+1) + ".rank." + j + ".dqn";
    		    			LOGGER.info("copying " + tmpDqnPath + " to " + saveDqnPath + " -- " + survivorStats.playStats.toString());
    		    	        Files.copy(new File(tmpDqnPath), new File(saveDqnPath));
    	        		}
	    	        winningStats = scoreCard.get(0);
	    			LOGGER.info("generation " + (i+1) + "winner: " + winningStats.toString());
	    	        generationDqnFilePath = localConfig.targetDirectoryForEvolution +"/" + MODELS_DIR + "/evolve.generation." + (i+1) + ".dqn";
	    			LOGGER.info("copying " + winningStats.stats.player1TargetDqnPath + " to " + generationDqnFilePath);
	    	        Files.copy(new File(winningStats.stats.player1TargetDqnPath), 
	    	        				new File(generationDqnFilePath));
	    			LOGGER.info("recycling generation as " + generationDqnFilePath);
    	        }
    		}
        
    		LOGGER.info("original source DQN was: " + localConfig.motherDqnFilePath);
    		if (winningStats!=null) {
    			LOGGER.info("final output is: " + generationDqnFilePath);
    			winningStats.stats.player1TargetDqnPath = generationDqnFilePath;
    		}
    		return winningStats;
    }
    
    private List<ScoredStats> runGeneration(Config localConfig, 
				  String generationDqnFilePath, 
				  GummyConfig gummyConfig) throws IOException {
    	
    		// Learning
		List<GummyRunner.GummyRunnerStats> statsList = runBatch(localConfig,
				generationDqnFilePath,
				gummyConfig);
		
		// Filter for complete flunkers
		List<ScoredStats> scoreCard = scoreGeneration(statsList, localConfig.filterHandsPerProgeny);
		
		// re-run with survivors
		if (scoreCard.size()>0) {
			statsList.clear();
			for (ScoredStats sstats : scoreCard) {
				statsList.add(sstats.stats);
			}
			scoreCard = scoreGeneration(statsList, localConfig.validationHandsPerProgeny);
		}

		// report
		if (scoreCard.size()>0) {
			ScoredStats winningStats = scoreCard.get(0);
			LOGGER.info("=-=-=-= AND THE WINNER IS:");
			LOGGER.info(winningStats.toString());  
			LOGGER.info("=-=-=-= AND HOW ABOUT A ROUND OF APPLAUSE FOR ALL OUR FANTASTIC CONTESTANTS:");
			for (int j=1; j<scoreCard.size();j++) {
				ScoredStats scoredStats = scoreCard.get(j);
				LOGGER.info("=-=-=-= runner up number " + j);
				LOGGER.info(scoredStats.toString());		
			}
		}
		
		return scoreCard;
    }
    
    protected List<GummyRunner.GummyRunnerStats> runBatch(Config localConfig, 
						  String dqnPath, 
						  GummyConfig gummyConfig) throws IOException {
    	
    		File tmpDir = Files.createTempDir();
    		tmpDir.deleteOnExit();
    		File dqnFile = null;
    		if (dqnPath!=null) {
    			dqnFile = new File(dqnPath);
    		}
    		
        List<GummyRunner.GummyRunnerStats> statsList = new ArrayList<GummyRunner.GummyRunnerStats>();
        
        gummyConfig.setApproxHandsToPlay(localConfig.learningHandsPerProgeny);
        gummyConfig.setGreedyAnnealHands(localConfig.stochasticHandsPerLearning);
        
        for (int i=0; i<localConfig.progenyPerGeneration; i++) {
        		String runnerPath = null;
        		if (dqnFile != null) {
        			File f = new File(tmpDir, dqnFile.getName() + "GummyNetworkEvolver.dqnCopy." + i);
        			Files.copy(dqnFile, f);
        			runnerPath = f.getPath();
        		}
        		
        		GummyRunnerConfig runnerConfig = getGenerationRunnerConfig(localConfig.learningHandsPerProgeny, 
       				gummyConfig, 
       				tmpDir, 
       				"CONFIG",
       				false,
       				localConfig.useBot);                       
        		runnerConfig.player1DqnPath = runnerPath;
        		if (localConfig.useBot) {
        			runnerConfig.player2DqnPath = "bot";
        			runnerConfig.player2Name = "bot";
        		} else {
        			runnerConfig.player2DqnPath = runnerPath;
        		}
        		
        		GummyRunner runner = new GummyRunner();
        		runner.defaultModelSavePath = localConfig.targetDirectoryForEvolution + "/" + GummyNetworkEvolver.MODELS_DIR;
        		GummyRunner.GummyRunnerStats runnerStats = runner.gummyRunner(runnerConfig);
        		statsList.add(runnerStats);
        		LOGGER.info("******** GummyNetworkEvolver: Network " + i + " **********");
        		LOGGER.info(runnerStats.toString());
        		LOGGER.info("**************************************************");
        }
        
        report(statsList);
        return statsList;
        
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
    
    protected void report(List<GummyRunner.GummyRunnerStats> statsList) {
		LOGGER.info(String.format("Model %5s: %4s %5s %7s %5s %5s",
				"num", 
				"winR", 
				"turns", 
				"rewrd", 
				"drawD", 
				"voteD" 
				 ));
        for (int i=0; i<statsList.size(); i++) {
        		GummyRunner.GummyRunnerStats runnerStats = statsList.get(i);
        		GummySimulatorStats stats = runnerStats.simulatorStats;
        		LOGGER.info(String.format("Model %5d: %02.2f %03.2f %05.2f %02.2f %02.2f",
        					i,
        					stats.getWinRatio()*100.0, 
        					stats.avgHandSteps/10.0, 
        					stats.avgHandReward, 
        					stats.getDrawRatio()*100.0, 
        					stats.getDiscardVoteRatio()*100.0 
        					 ));
        }
    }

    private class ScoredStats implements Comparable<ScoredStats> {
    		GummyRunner.GummyRunnerStats stats = null;
    		GummyDeepPlayGin.Stats playStats = null;
    		double score = 0;
		@Override
		public int compareTo(ScoredStats o) {return Double.compare(((ScoredStats)o).score, this.score);}
		@Override
		public String toString() {
			String rez = "score=" + score;
			if (playStats!=null) rez += "\n" + playStats.toString();
			if (stats!=null) rez += "\n" + stats.toString();
			return rez; 
		}
	}
    
    protected List<ScoredStats> scoreGeneration(List<GummyRunner.GummyRunnerStats> statsList, int validationHands) {
		int candidateCount = statsList.size();
		List<ScoredStats> scoreCard = new ArrayList<ScoredStats>();
		List<Integer> winsList = new ArrayList<Integer>();
		List<Integer> turnsList = new ArrayList<Integer>();
		List<Double> ratioList = new ArrayList<Double>();
        for (int i=0; i<candidateCount; i++) {
        		GummyRunner.GummyRunnerStats runnerStats = statsList.get(i);
        		String model = runnerStats.player1TargetDqnPath;
    			LOGGER.info("PLAY - validating model " + i + ": " + model);
        		GummyDeepPlayGin.Stats playStats = GummyDeepPlayGin.play(model, "bot", validationHands);
    			LOGGER.info("PLAY - model " + i + ": " + playStats.winCounts[0] + " model wins -- " + playStats.toString());
        		if (playStats.drawDeckCount == 0 || playStats.drawPileCount==0) {
        			// fell in the hole. Forget it.
        			continue;
        		}
        		ScoredStats stats = new ScoredStats();
        		stats.stats = runnerStats;
        		stats.playStats = playStats;
        		scoreCard.add(stats);
        		winsList.add(playStats.winCounts[0]);
        		turnsList.add(turnEntry(playStats.turns())); // reverse, lower is better
        		ratioList.add(ratioEntry(playStats)); // closest to 0.75%, I guess        		
        }
        Collections.sort(winsList);
        Collections.sort(turnsList);
        Collections.sort(ratioList);
        for (int i=0; i<scoreCard.size(); i++) {
    			ScoredStats scoredStats = scoreCard.get(i);
        		GummyDeepPlayGin.Stats playStats = scoredStats.playStats;
        		int player1Wins = playStats.winCounts[0];
        		//int turnEntry = turnEntry(playStats.turns());
        		//double ratioEntry = ratioEntry(playStats);
        		for (int j=0; j<winsList.size(); j++) {
        			if (winsList.get(j) == player1Wins) {
        				scoredStats.score += j;
        				player1Wins = -1; // no dups
        			}
        			//if (turnsList.get(j) == turnEntry) {
        			//	scoredStats.score += j;
        			//	turnEntry = -1; // no dups
        			//}
        			//if (ratioList.get(j) == ratioEntry) {
        			//	scoredStats.score += j;
        			//	ratioEntry = -10000; 
        			//}
        		}
        }
        
        Collections.sort(scoreCard);
        
        return scoreCard;
    }
    
    private int turnEntry(int turns) {
    		return 200-turns;
    }
    
    private double ratioEntry(GummyDeepPlayGin.Stats playStats) {
		double ratio = (playStats.drawPileCount + playStats.voteDiscardCount)/(double)(playStats.totalDecisions());
		return 1-Math.abs(0.75 - ratio);
    }
    
    protected static Logger LOGGER = Logger.getLogger(GummyNetworkEvolver.class.getName()); 
    private static void configLogger(Logger logger, String logPath) {try {Handler handler=new FileHandler(logPath); handler.setFormatter(new SimpleFormatter()); logger.addHandler(handler);} catch (Exception e) {e.printStackTrace();}}
	// static {configLogger(LOGGER, LOCAL_LOG_PATH);}
    private void fixupLogging(File logsDir) {
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
    
    protected List<ScoredStats> scoreGenerationOLD(List<GummyRunner.GummyRunnerStats> statsList) {
		int numRunners = statsList.size();
		List<ScoredStats> scoreCard = new ArrayList<ScoredStats>(numRunners);
		List<Double> winRatioList = new ArrayList<Double>(numRunners);
		List<Double> turnsList = new ArrayList<Double>(numRunners);
		List<Double> rewardList = new ArrayList<Double>(numRunners);
		List<Double> drawRatioList = new ArrayList<Double>(numRunners);
		List<Double> voteRatioList = new ArrayList<Double>(numRunners);
		
	    for (int i=0; i<numRunners; i++) {
	    		GummyRunner.GummyRunnerStats runnerStats = statsList.get(i);
	    		GummySimulatorStats stats = runnerStats.simulatorStats;
	    		winRatioList.add(stats.getWinRatio()); 
	    		turnsList.add(stats.avgHandSteps*1.0); 
	    		rewardList.add(stats.avgHandReward); 
	    		drawRatioList.add(stats.getDrawRatio()); 
	    		voteRatioList.add(stats.getDiscardVoteRatio()); 
	    }
	    Collections.sort(winRatioList);
	    Collections.sort(turnsList);
	    Collections.reverse(turnsList); // lower is better
	    Collections.sort(rewardList);
	    Collections.sort(drawRatioList);
	    Collections.sort(voteRatioList);
	    for (int i=0; i<numRunners; i++) {
	    		double score = 0;
	    		GummyRunner.GummyRunnerStats runnerStats = statsList.get(i);
	    		GummySimulatorStats stats = runnerStats.simulatorStats;
	    		for (int j=0; j<numRunners; j++) {
	    			if (winRatioList.get(j)==stats.getWinRatio()) score+=j;
	    			if (turnsList.get(j)==stats.avgHandSteps*1.0) score+=j;
	    			if (rewardList.get(j)==stats.avgHandReward) score+=j;
	    			if (drawRatioList.get(j)==stats.getDrawRatio()) score+=j/2.0;
	    			if (voteRatioList.get(j)==stats.getDiscardVoteRatio()) score+=j/2.0;
	    		}
	        ScoredStats scoredStats = new ScoredStats(); 
	        scoredStats.stats = runnerStats;
	        scoredStats.score = score;
	    		scoreCard.add(scoredStats);
	    }
    
    		Collections.sort(scoreCard);
		return scoreCard;
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
