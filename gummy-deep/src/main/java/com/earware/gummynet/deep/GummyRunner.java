package com.earware.gummynet.deep;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.ILearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.network.dqn.ExposedNetworkDQN;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManager;
import org.deeplearning4j.util.ModelSerializer;

/**
 * Gin Rummy Reinforcement Learning
 */
public class GummyRunner {
	
    /////////////////////////////////////////////////////
    
    public QLearning.QLConfiguration GUMMY_QLEARNING_CONFIG;
  
    // DQN == "Deep Q-Network"
    // A deep Q network (DQN) is a multi-layered neural network that
    // selects actions given an input state. The network 
    // evaluates its input state s and outputs a vector of 
    // action values Q(s,...; theta), which specify the "probability" 
    // reccommendation-weight of the action
    // (where theta are the parameters of the network.)
    public DQNFactoryStdDense.Configuration GUMMY_NET_CONFIG;

    // /////////////////////////////////////////////
	public String defaultModelSavePath = GummyNetworkEvolver.MODELS_DIR;
	private static String defaultModelSavePrefix = "GummyDeep";
	
	private GummyRunnerConfig runnerConfig = null;
    
    // /////////////////////////////////////////////
    public class GummyRunnerStats {
    		GummyRunnerConfig runnerConfig;
    		GummySimulatorStats simulatorStats = null;
    		GummyDeepPlayGin.Stats playGinStats = null;
    		String player1TargetDqnPath = null;
    		String player2TargetDqnPath = null;
    		
    		@Override
    		public String toString() {
    			String s = "player1TargetDqnPath=" + player1TargetDqnPath + '\n'
    					+ "player2TargetDqnPath=" + player2TargetDqnPath;
    			if (runnerConfig!=null) {
    				s+= '\n' + runnerConfig.toString();
    			}
    			if (simulatorStats!=null) {
    				s+= '\n' + simulatorStats.toString();
    			}
    			return s; 
    		}
    }
	
    public GummyRunnerStats gummyRunner(GummyRunnerConfig cfg) throws IOException {
		this.runnerConfig = cfg;
		GummyRunnerStats stats = null;

		if (runnerConfig.player1IsTraining && runnerConfig.player2IsTraining) {
			stats = runBothTraining();
		} else if (!runnerConfig.player1IsTraining && !runnerConfig.player2IsTraining) {
			stats = runNoTraining();
		} else {
			stats = runOneTraining();
		}
        	return stats;
    }
    
    // **********************
    public GummyRunnerStats runNoTraining() throws IOException {
        GummyRunnerStats runnerStats = new GummyRunnerStats();

    		GummyMDP.Rewards rewards1 = new GummyMDP.Rewards();
    		GummyMDP.Rewards rewards2;
    		if (runnerConfig.player1ConfigPath!=null 
    				&& runnerConfig.player1ConfigPath.length()>0) {
    			rewards1 = GummyConfig.fromFile(runnerConfig.player1ConfigPath).rewards;
    		}
    		if (runnerConfig.player2ConfigPath!=null 
    				&& runnerConfig.player2ConfigPath.length()>0) {
    			rewards2 = GummyConfig.fromFile(runnerConfig.player2ConfigPath).rewards;
    		} else {
    			rewards2 = rewards1;
    		}
    		
    		GummyDeepPlayGin.Stats stats = GummyDeepPlayGin.play(runnerConfig.player1DqnPath, 
    															rewards1, 
    															runnerConfig.player2DqnPath, 
    															rewards2, 
    															200);
        runnerStats.playGinStats = stats;

        return runnerStats;
    }
    
    // **********************
    public GummyRunnerStats runOneTraining() throws IOException {
        GummyRunnerStats stats = new GummyRunnerStats();
        stats.runnerConfig = runnerConfig;
        
        // see who's training and who's not
        String trainingCfgPath;
        String trainingDqnPath;
        int trainingIndex = 0;
        String playingCfgPath;
        String playingDqnPath;
        if (runnerConfig.player1IsTraining) {
            trainingIndex = 0;
        		trainingCfgPath = runnerConfig.player1ConfigPath;
        		trainingDqnPath = runnerConfig.player1DqnPath;
        		playingCfgPath = runnerConfig.player2ConfigPath;
        		playingDqnPath = runnerConfig.player2DqnPath;
        } else {
            trainingIndex = 1;
        		trainingCfgPath = runnerConfig.player2ConfigPath;
        		trainingDqnPath = runnerConfig.player2DqnPath;
        		playingCfgPath = runnerConfig.player1ConfigPath;
        		playingDqnPath = runnerConfig.player1DqnPath;
        }
        
		GummyConfig trainingConfig = GummyConfig.fromFile(trainingCfgPath);
        trainingConfig.setApproxHandsToPlay(runnerConfig.approxNumberOfHandsToPlay);
		
        //define the simulator
        GummySimulator gummySimulator = new GummySimulator(trainingConfig.inputWeights, 
        				runnerConfig.player1Name, runnerConfig.player2Name);
        
        // training
		setConfigs(trainingConfig);
		DQN<?> trainingDqn = getDQN(trainingDqnPath);
		GummyMDP trainingMDP = new GummyComboMDP(gummySimulator, trainingConfig.rewards);
        
        // playing
		GummyConfig playingConfig = trainingConfig;
		if (playingCfgPath!=null && playingCfgPath.length()>0) {
			playingConfig = GummyConfig.fromFile(playingCfgPath);
		}
		playingConfig.setApproxHandsToPlay(runnerConfig.approxNumberOfHandsToPlay);
		GummyMDP playingMDP = null;
		if (playingDqnPath!=null && playingDqnPath.equals("bot")) {
			// use a bot, not a DQN
			GummyGinStrategyMDP mdp = new GummyGinStrategyMDP(gummySimulator);
			mdp.strategy = new GummySmartGinStrategy(playingConfig.rewards);
			playingMDP = mdp;
		} else {
			setConfigs(playingConfig);
			playingMDP = new GummyDeepPlayerMDP(gummySimulator, playingDqnPath);
		}

		// assign to simulator
		if (trainingIndex==0) {
			gummySimulator.init(trainingMDP, playingMDP);
		} else {
			gummySimulator.init(playingMDP, trainingMDP);
		}
        
        //record the training data in rl4j-data in a new folder
        DataManager manager = new DataManager();
        
        // launch UI if needed
        if (trainingConfig.useUI) {
    			GummyDeepUI.startUI((MultiLayerNetwork)
    					new ExposedNetworkDQN<>(trainingDqn).getNeuralNetworks()[0]);
        }

        //define the training
        trainingConfig.setApproxHandsToPlay(runnerConfig.approxNumberOfHandsToPlay);
		setConfigs(trainingConfig);
        ILearning<GummyState, Integer, DiscreteSpace> qlearning
        			= createLearning(trainingMDP, trainingDqn, manager);
        
        //start the training
        train(qlearning);

        stats.simulatorStats = gummySimulator.getStats();
        
        // save
        String targeDqnPath = saveDQN(trainingDqn, trainingDqnPath);
        if (trainingIndex==0) {
        		stats.player1TargetDqnPath = targeDqnPath;
        } else {
    			stats.player2TargetDqnPath = targeDqnPath;
        }

        //good practice!
        gummySimulator.close(); 
        
        LOGGER.info("*********** FINAL REPORT *********");
        LOGGER.info(stats.toString());
        LOGGER.info("**********************************");
        
		return stats;
    }
    
    // **********************
    public GummyRunnerStats runBothTraining() throws IOException {
        GummyRunnerStats stats = new GummyRunnerStats();
        stats.runnerConfig = runnerConfig;
        
		if (runnerConfig.useSingleDqn) {
			fixupConfigForSingleDQN();
		}
        
    		GummyConfig gummyConfig1 = GummyConfig.fromFile(runnerConfig.player1ConfigPath);
    		gummyConfig1.setApproxHandsToPlay(runnerConfig.approxNumberOfHandsToPlay);
    		setConfigs(gummyConfig1);
		DQN<?> dqn1 = getDQN(runnerConfig.player1DqnPath);
		
		GummyConfig gummyConfig2 = GummyConfig.fromFile(runnerConfig.player2ConfigPath);
		gummyConfig2.setApproxHandsToPlay(runnerConfig.approxNumberOfHandsToPlay);
		DQN<?> dqn2 = null;
		if (runnerConfig.useSingleDqn 
				&& ((runnerConfig.player2DqnPath==null && runnerConfig.player1DqnPath==null) 
						|| runnerConfig.player2DqnPath.equals(runnerConfig.player1DqnPath))) {
			dqn2 = dqn1;
		} else {
    			setConfigs(gummyConfig2);
			dqn2 = getDQN(runnerConfig.player2DqnPath);
		}
		
        //record the training data in rl4j-data in a new folder
        DataManager manager = new DataManager();

        //define the simulator
        GummySimulator gummySimulator = new GummySimulator(gummyConfig1.inputWeights, 
        				runnerConfig.player1Name, runnerConfig.player2Name);
        gummySimulator.init(new GummyComboMDP(gummySimulator, gummyConfig1.rewards), 
        						new GummyComboMDP(gummySimulator, gummyConfig2.rewards));

        if (gummyConfig1.useUI) {
        		GummyDeepUI.startUI((MultiLayerNetwork)
        				new ExposedNetworkDQN<>(dqn1).getNeuralNetworks()[0]);
        }
        //if (gummyConfig2.useUI) {
        //		org.deeplearning4j.ui.play.PlayUIServer.DEFAULT_UI_PORT=9090;
    		//	GummyDeepUI.startUI((MultiLayerNetwork)
    		//			new ExposedNetworkDQN<>(dqn2).getNeuralNetworks()[0]);
        //}
        
        //define the trainings
        ILearning<GummyState, Integer, DiscreteSpace> qlOne
        			= createLearning(gummySimulator.getSimOne().mdp, dqn1, manager);
        ILearning<GummyState, Integer, DiscreteSpace> qlTwo
        			= createLearning(gummySimulator.getSimTwo().mdp, dqn2, manager);

        //start the training
        train(qlOne, qlTwo);

        stats.simulatorStats = gummySimulator.getStats();
        
        // save
        stats.player1TargetDqnPath = saveDQN(dqn1, runnerConfig.player1DqnPath);
        if (!dqn1.equals(dqn2)) {
        		stats.player2TargetDqnPath = saveDQN(dqn2, runnerConfig.player2DqnPath);
        } else {
        		stats.player2TargetDqnPath = stats.player1TargetDqnPath;
        }

        //good practice!
        gummySimulator.close(); 
        
        LOGGER.info("*********** FINAL REPORT *********");
        LOGGER.info(stats.toString());
        LOGGER.info("**********************************");
        
        return stats;
    }
    
    private void fixupConfigForSingleDQN() {
		String dqnPath = runnerConfig.player1DqnPath;
		if (dqnPath==null || dqnPath.length()==0) {
			dqnPath = runnerConfig.player2DqnPath;
		}
		String configPath = runnerConfig.player1ConfigPath;
		if (configPath==null || configPath.length()==0) {
			configPath = runnerConfig.player2ConfigPath;
		}
		runnerConfig.player1DqnPath=dqnPath;
		runnerConfig.player2DqnPath=dqnPath;
		if (runnerConfig.player1ConfigPath==null || runnerConfig.player1ConfigPath.length()==0) {
			runnerConfig.player1ConfigPath = configPath;
		}
		if (runnerConfig.player2ConfigPath==null || runnerConfig.player2ConfigPath.length()==0) {
			runnerConfig.player2ConfigPath = configPath;
		}
    }
    
    // /////////////////////////////////////
    
    private DQN<?> getDQN(String path) {
		DQN<?> dqn = null;
		if (path!=null) {
			dqn = loadDQN(path);
		} else {
			GummyMDP mdp = new GummyComboMDP();
			dqn = createDQN(mdp.getObservationSpace().getShape(),mdp.getActionSpace().getSize());
		}
		return dqn;
    }

    private DQN<?> createDQN(int[] obsSpaceShape, int actionSpaceSize) {
		DQN<?> dqn = new DQNFactoryStdDense(GUMMY_NET_CONFIG)
				.buildDQN(obsSpaceShape,actionSpaceSize);
		return dqn;
    }

    private String saveDQN(IDQN<?> dqn, String sourcePath) {
    		String dqnPath;
    		dqnPath = generateTargetDQNPath(sourcePath); 
    		
		try {
			File targetDirectory = new File(dqnPath).getParentFile();
    			if (targetDirectory!=null && !targetDirectory.exists()) {targetDirectory.mkdirs();}
    		
			LOGGER.info("saving DQN to  " + dqnPath);
    			dqn.save(dqnPath);
    		} catch (IOException e) {
    			LOGGER.severe("Cannot save DQN: " + e.getMessage());
    		} catch (SecurityException e) {
    			LOGGER.severe("Cannot save DQN: " + e.getMessage());
    		}
		
		return dqnPath;
    }
    
    @SuppressWarnings("rawtypes")
	private static DQN<?> loadDQN(String path) {
    		DQN<?> loadedDQN = null;
    		MultiLayerNetwork mln = null;
    		try {
    			mln = ModelSerializer.restoreMultiLayerNetwork(path, true);
    		} catch (IOException e) {
    			LOGGER.severe("Cannot load DQN: " + e.getMessage());
    			throw new IllegalArgumentException("IOException trying to load DQN",e); 
		}
    		loadedDQN = new DQN(mln);
    		return loadedDQN;
    }

	private ILearning<GummyState, Integer, DiscreteSpace> createLearning(GummyMDP mdp, IDQN<?> dqn, DataManager manager) {
        //return new QLearningDiscreteDense<GummyState>(mdp, 
		//		GUMMY_NET, GUMMY_QL_CONFIG, manager);
        return new GummyQLearning<GummyState>(mdp,
        					dqn, 
        					GUMMY_QLEARNING_CONFIG,
                        manager);
	}
    
    private void train(ILearning<GummyState, Integer, DiscreteSpace> qlearning) {
    		// train just one network
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Object[] learnings = {qlearning};
        try {
        		GummyMDP mdp = (GummyMDP)(qlearning.getMdp());
        		executorService.execute(createRunnable(qlearning, mdp, learnings));
                
        		executorService.shutdown();
        		executorService.awaitTermination(30, TimeUnit.DAYS);
        	
        } catch (InterruptedException e) {
    			return; 
        }
    }
    
    private void train(ILearning<GummyState, Integer, DiscreteSpace> qlearningOne, 
    						ILearning<GummyState, Integer, DiscreteSpace> qlearningTwo) {
    	
        //start the training in 2 threads
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Object[] learnings = {qlearningOne,qlearningTwo};
        
        try {
        		GummyMDP mdp;
        	
    	        mdp = (GummyMDP)(qlearningOne.getMdp());
	        executorService.execute(createRunnable(qlearningOne, mdp, learnings));
	        	Thread.sleep(10000);
	        
	        executorService.execute(createRunnable(qlearningTwo, mdp, learnings));
	        mdp = (GummyMDP)(qlearningTwo.getMdp());
	        
	        executorService.shutdown();
	        	executorService.awaitTermination(30, TimeUnit.DAYS);
	        	
        } catch (InterruptedException e) {
        		return; 
        }
    }
    
    private static Runnable createRunnable(final ILearning<GummyState, Integer, DiscreteSpace> learning,
    													final GummyMDP gummyMDP, final Object[] learnings) {
    		return new Runnable() {
				@Override
				public void run() {
					try {
						learning.train();
					} catch (Exception e) {
						LOGGER.severe(e.getLocalizedMessage());
					}
			        LOGGER.info("cleaning up: " + gummyMDP);
			        // someone is done - drain the other learnings remaining steps so they end also
			        for (Object o: learnings) {
			        		@SuppressWarnings("unchecked")
						GummyQLearning<GummyState> learning 
								= (GummyQLearning<GummyState>)o;
			        		learning.cancel();
			        	}
					gummyMDP.getGummySimulator().latchFinal(null);
				}};
    }
    
    private void setConfigs(GummyConfig config) {
        GUMMY_QLEARNING_CONFIG =
                new QLearning.QLConfiguration(
                        1234,   //Random seed
                        config.getMaxStepsPerHandQL(),    
                        config.getMaxStepsOverallQL(),   
                        config.getMaxStepsPerHandQL()*100, //Max size of experience replay
                        32,    //size of batches
                        config.getHardTargetUpdate(),   
                        0,     //num step noop warmup
                        config.getRewardScaling(),   
                        config.getGamma(),  
                        config.getTdErrorClipping(),
                        config.getMinEpsilon(),  
                        config.getGreedyAnnealSteps(),  
                        true   //double DQN
                );

       GUMMY_NET_CONFIG =
                 DQNFactoryStdDense.Configuration.builder()
                 		.l2(config.neuralNetL2)
                 		.learningRate(config.neuralNetLearningRate)
                 		.numLayer(config.neuralNetNumLayers)
                 		.numHiddenNodes(config.neuralNetNodesPerLayer)
                 		.build();

    }
    
    private String generateTargetDQNPath(String sourcePath) {
    		String defaultModelSaveTag = Long.toString(System.currentTimeMillis()).substring(8);
    		if (sourcePath==null || sourcePath.length()==0) {
    			return defaultModelSavePath + File.separator 
    					+ defaultModelSavePrefix 
    					+ "." + defaultModelSaveTag 
    					+ "." + "dqn";
    		} else { 
    			//return defaultModelSavePath + File.separator 
    			//		+ sourcePath 
    			//		+ "." + defaultModelSaveTag 
    			//		+ "." + "dqn";
    			return sourcePath;
    		}
    }
    
    public static void dumpConfig(GummyConfig config, String dqnPath) {
		String sourceFiles = "";
		if (dqnPath!=null) {
			sourceFiles = '\n' + "drawDQN loaded from: " + dqnPath 
			        ;
		}
		LOGGER.info(         "****** GummyRunner CONFIG *********"
					+ sourceFiles
					+ '\n' + config.toString()
					+ '\n' + "***********************************"
					);
    }
    
    // ///////////////////////////////
    public static void main(String[] args) throws IOException {
        LOGGER.info("executed at: " + new Date());
        GummyRunnerConfig runnerConfig = null;
        if (args.length<1) {
    			System.err.println("no runner configuration provided");
    			return;
        }
        	LOGGER.info("loading config from: " + args[0]);
        	runnerConfig = GummyRunnerConfig.fromFile(args[0]);
        LOGGER.info(runnerConfig.toString());
        new GummyRunner().gummyRunner(runnerConfig);
    }
    
    public static Logger LOGGER = Logger.getLogger(GummyRunner.class.getPackage().getName()); 
    // static {try {Handler handler=new FileHandler("logs/GummyDeep.log"); handler.setFormatter(new SimpleFormatter()); LOGGER.addHandler(handler);} catch (Exception e) {e.printStackTrace();}}
}
