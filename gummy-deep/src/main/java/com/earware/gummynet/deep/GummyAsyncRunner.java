package com.earware.gummynet.deep;

import java.io.IOException;
import java.util.logging.Logger;

import org.deeplearning4j.rl4j.learning.async.nstep.discrete.AsyncNStepQLearningDiscrete;
import org.deeplearning4j.rl4j.learning.async.nstep.discrete.AsyncNStepQLearningDiscreteDense;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.util.DataManager;

public class GummyAsyncRunner {
	static GummyConfig config = new GummyConfig();
	
    public static AsyncNStepQLearningDiscrete.AsyncNStepQLConfiguration GUMMY_ASYNC_QL_CONFIG =
            new AsyncNStepQLearningDiscrete.AsyncNStepQLConfiguration(
                    1234,        //Random seed
                    config.getMaxStepsPerHandQL(), 
                    config.getMaxStepsOverallQL(), 
                    8,          //Number of threads
                    5,          //t_max
                    config.getHardTargetUpdate(),
                    0,          //num step noop warmup
                    config.getRewardScaling(),
                    config.getGamma(),
                    config.getTdErrorClipping(),
                    config.getMinEpsilon(),
                    config.getGreedyAnnealSteps()
            );
    public static DQNFactoryStdDense.Configuration GUMMY_NET_CONFIG;
	
	// //////////////////////////////////////
    public static void gummyAsyncNstep() throws IOException {

        //record the training data in rl4j-data in a new folder
        DataManager manager = new DataManager();

        //define the simulator
        GummySimulator gummySimulator = new GummySimulator(); 

        //define the trainings
        AsyncNStepQLearningDiscreteDense<GummyState> qlOne 
        			= createAsyncLearning(gummySimulator.getSimOne().mdp, manager);
        AsyncNStepQLearningDiscreteDense<GummyState> qlTwo 
				= createAsyncLearning(gummySimulator.getSimTwo().mdp, manager);

        // enable some logging for debug purposes on mdp
        //mdp.setFetchable(dql);
        //mdp.setFetchable(dql);
        //mdp.setFetchable(dql);
        //mdp.setFetchable(dql);

        //start the training in multiple threads
        train(qlOne, qlTwo);
        
        //good practice!
        gummySimulator.close();
        
        LOGGER.info("*********** FINAL REPORT *********");
        LOGGER.info(gummySimulator.getStats().toString());
        LOGGER.info("**********************************");
    }
    
    private static void train(Object a, Object b) {
    	
    }
    
    public static AsyncNStepQLearningDiscreteDense<GummyState> createAsyncLearning(GummyMDP mdp, DataManager manager) {
        return new AsyncNStepQLearningDiscreteDense<GummyState>(mdp, 
					GUMMY_NET_CONFIG, 
					GUMMY_ASYNC_QL_CONFIG, 
					manager);
    }
    protected static final Logger LOGGER = Logger.getLogger(GummyAsyncRunner.class.getPackage().getName()); 
}
