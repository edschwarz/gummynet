package com.earware.gummynet.deep;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

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
	private static GummyNetworkEvolver latestInstance=null;
	public static GummyNetworkEvolver getLatestInstance() {return latestInstance;}
	public GummyNetworkEvolver() {latestInstance=this;}
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
    public Stats evolve(GummyNetworkEvolver.Config config) throws IOException {
		LOGGER.info("OKOKOKOKOKOKOKOKOKKKK evolving: " + config.toString());
    		return evolveDirectory(config);
    }
    
    protected Stats evolveDirectory(GummyNetworkEvolver.Config config) throws IOException {
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
			if (!motherFile.exists() || !motherFile.canRead()) {
				throw new IllegalArgumentException("motherDqnFilePath \"" + config.motherDqnFilePath + "\": cannot find or read this file");
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
		
		GNELogging.fixupLogging(logsDir);
		LOGGER.info("evolution begins: " + config.toString());
		
		localConfig.gummyConfigFilePath = targetDir.getPath() + "/" + LOCAL_GUMMY_CONFIG_PATH;
		if (config.gummyConfigFilePath!=null && config.gummyConfigFilePath.length()>0) {
			fileCopy(new File(config.gummyConfigFilePath), new File(localConfig.gummyConfigFilePath));
		} else {
			// saving snapshot of default config
			new GummyConfig().save(localConfig.gummyConfigFilePath);
		}
		if (config.motherDqnFilePath!=null && config.motherDqnFilePath.length()>0) {
			File motherDqnFile = new File(config.motherDqnFilePath);
			if (motherDqnFile.isFile()) {
				localConfig.motherDqnFilePath = targetDir.getPath() + "/" + LOCAL_MOTHER_DQN_PATH;
				fileCopy(motherDqnFile, new File(localConfig.motherDqnFilePath));
			} else {
				File poolDir = poolCopy(motherDqnFile, new File(targetDir.getPath() + "/" + MODELS_DIR));
				localConfig.motherDqnFilePath = poolDir.getPath();
			}
		}
		
		Stats gneStats = evolveLocal(localConfig);
		
		LOGGER.info("evolution completed: " + gneStats.toString());
		if (localConfig.evolvedDqnFilePath==null || localConfig.evolvedDqnFilePath.length()==0) {
		localConfig.evolvedDqnFilePath = targetDir.getPath() + "/" + LOCAL_EVOLVED_DQN_PATH;
		}
		LOGGER.info("copying " + gneStats.modelStats.get(0).dqnPath + " to " + localConfig.evolvedDqnFilePath);
		fileCopy(new File(gneStats.modelStats.get(0).dqnPath), 
				new File(localConfig.evolvedDqnFilePath));
	
		return gneStats;
    }
    
    protected File fileCopy(File source, File target) throws IOException {
    		return Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING).toFile();
    }

    protected File poolCopy(File sourceDir, File poolDir) throws IOException {
    		final List<Integer> indexWrapper = new ArrayList<Integer>();
    		indexWrapper.add(0);
    		Files.walkFileTree(sourceDir.toPath(), 
    							new HashSet<FileVisitOption>(), 
    							1, 
    							new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                		File f = file.toFile();
                		if (f.isFile()) {
                			Integer index = indexWrapper.get(0);
                			fileCopy(f, new File(poolDir, "seed" + (++index) + ".dqn"));  // f.getName()));
                			indexWrapper.set(0,index);
                		}
                    return FileVisitResult.CONTINUE;
                }});
    		return poolDir;
    }
    
    protected File createTempDir() throws IOException {
    		return Files.createTempDirectory(
    				"GNEtmp" + System.currentTimeMillis()).toFile();

    }

    GNEParentPool parentPool = null; 
    Stats gneStats = null;
    public Stats getStats() { updateStats(); return gneStats; }
    public String getScoreboardString(int howFarBack) { return parentPool!=null? 
    		parentPool.getScoreboard().scoreboardString(howFarBack) 
    		: ""; }
    private void updateStats() {
    	if (gneStats !=null) {
    		gneStats.modelStats = parentPool.getStatsList();
    	}
    }
    
    protected Stats evolveLocal(GummyNetworkEvolver.Config localConfig) throws IOException {
    	
		LOGGER.info("evolving with local GNE config: " + localConfig.toString());
    	
		// fix up learning config
		GummyConfig templateConfig = GummyConfig.fromFile(localConfig.gummyConfigFilePath);
		templateConfig.setApproxHandsToPlay(localConfig.learningHandsPerProgeny);
		templateConfig.setGreedyAnnealHands(localConfig.stochasticHandsPerLearning);
		LOGGER.info("evolving with local Gummy config: " + templateConfig.toString());

		// set up temp directory
		parentPool = new GNEParentPool(new File(localConfig.targetDirectoryForEvolution + "/" + MODELS_DIR));
		gneStats = new Stats();
		gneStats.startTime = System.currentTimeMillis();

		// set up temp directory
		File tmpDir = createTempDir();
		tmpDir.deleteOnExit();
		
		// baseline input model, if any
		if (localConfig.motherDqnFilePath!=null) {
			initPool(localConfig.motherDqnFilePath, localConfig.validationHandsPerProgeny, parentPool);
		}
				
		for (int progeny=1; progeny<=localConfig.progenyPerGeneration; progeny++) {
			
			// spawn progeny model        		
			String progenyPath = null;
			String parentDqnPath = parentPool.getParentFromPool();
    		if (parentDqnPath != null) {
    			File parentDqnFile = new File(parentDqnPath);
    			File f = new File(tmpDir, parentDqnFile.getName() + "GummyNetworkEvolver.dqnCopy." + progeny);
    			fileCopy(parentDqnFile, f);
    			progenyPath = f.getPath();
    			LOGGER.info("SPAWN - model " + progeny + ": copied from source " + parentDqnPath);
    		} else {
    			LOGGER.info("SPAWN - model " + progeny + ": no source model, will create a new model");
    		}
    		gneStats.modelsSpawned++;

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
    		LOGGER.info("PROGENY - model " + progeny + ": " + "adding to pool - " + progenyPath + " as progeny of " + parentDqnPath);
    		parentPool.addProgeny(progenyPath, validateStats, parentDqnPath);
    		gneStats.progenyCreated++;
    		
    		LOGGER.info("***** SCOREBOARD\n" + getScoreboardString(40));     		
		}	
		
		updateStats();
		return gneStats;
    }

    protected boolean initPool(String motherDqnFilePath, int benchmarkHands, GNEParentPool pool) throws IOException {
    		if (motherDqnFilePath==null || motherDqnFilePath.length()==0 || benchmarkHands<1 || pool==null) {
    			return false;
    		}
    		File motherFile = new File(motherDqnFilePath);
    		if (motherFile.isDirectory()) {
    			LOGGER.info("initializing pool from " + motherDqnFilePath);
    			final List<Integer> totalAdded=new ArrayList<Integer>();totalAdded.add(0);
        		Files.walkFileTree(motherFile.toPath(), new HashSet<FileVisitOption>(), 1, 
        				new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    		File f = file.toFile();
                    		if (initPoolModel(f.getPath(), benchmarkHands, pool)) {
                    			totalAdded.set(0, totalAdded.get(0)+1);
                    		}
                        return FileVisitResult.CONTINUE;
                    }});
    			LOGGER.info("initialized " + totalAdded.get(0) + " models to pool from " + motherDqnFilePath);
    		} else {
    			boolean loadedOK = initPoolModel(motherDqnFilePath, benchmarkHands, pool);
    			if (!loadedOK) {
    				throw new IllegalArgumentException("cannot load file " + motherDqnFilePath + " as a DQN");
    			}
    		}
		return true;
    }
    
    private boolean initPoolModel(String motherDqnFilePath, int benchmarkHands, GNEParentPool pool) {
    		try {
    			String mother = motherDqnFilePath;
    			LOGGER.info("BASELINE - model " + mother + ": baselining begins");
    			GummyDeepPlayGin.Stats motherStats = GummyDeepPlayGin.play(mother, "bot", benchmarkHands, 0);
    			LOGGER.info("BASELINE - model " + mother + ": " + motherStats.winCounts[0] + " model wins -- " + motherStats.toString());
    			pool.addParent(mother, motherStats);
    		} catch (Exception e) {
    			return false;
    		}
		return true;
    }
        
    protected GummyRunner.GummyRunnerStats train( String dqnPath, 
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

    //******************************************************
    //******************************************************
    //******************************************************
    public static class Stats {
    		long startTime=System.currentTimeMillis();
    		int modelsSpawned=0;
    		int progenyCreated=0;
    		List<GNEParentPool.GneParentStats> modelStats;
    		public String toString() {return "GNE STATS: startTime=" + startTime + " modelsSpawned=" + modelsSpawned + " progenyCreated=" + progenyCreated;}
    }
    
    protected static Logger LOGGER = Logger.getLogger(GummyNetworkEvolver.class.getName()); 
}
