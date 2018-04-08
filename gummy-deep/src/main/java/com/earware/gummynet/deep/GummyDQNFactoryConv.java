package com.earware.gummynet.deep;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.rl4j.network.dqn.DQNFactory;
import org.deeplearning4j.rl4j.util.Constants;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.IUpdater;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * @author edward (ed.schwarz@earware.com) 2/22/18 from rubenfiszel (ruben.fiszel@epfl.ch) 
 * 
 */
@Value
public class GummyDQNFactoryConv implements DQNFactory {

	public static class GummyConvConf {
		// Layer
        protected String layerName = null;
        //protected List<LayerConstraint> allParamConstraints;
        //protected List<LayerConstraint> weightConstraints;
        //protected List<LayerConstraint> biasConstraints;
        //protected IDropout iDropout;
        
        // BaseLayer
        //protected IActivation activationFn = null;
        //protected WeightInit weightInit = null;
        protected double biasInit = Double.NaN;
        // protected Distribution dist = null;
        protected double l1 = Double.NaN;
        protected double l2 = Double.NaN;
        protected double l1Bias = Double.NaN;
        protected double l2Bias = Double.NaN;
        // protected IUpdater iupdater = null;
        // protected IUpdater biasUpdater = null;
        // protected GradientNormalization gradientNormalization = null;
        // protected double gradientNormalizationThreshold = Double.NaN;
        // protected IWeightNoise weightNoise;
        
        // FeedForwardLayer
		int nIn;
		int nOut;
		
		// ConvolutionLayer
	    protected boolean hasBias = true;
	    protected ConvolutionMode convolutionMode = ConvolutionMode.Truncate; //Default to truncate here - default for 0.6.0 and earlier networks on JSON deserialization
	    protected int dilation[] = new int[]{1,1};
	    protected int[] kernelSize; // Square filter
	    protected int[] stride; // Default is 2. Down-sample by a factor of 2
	    protected int[] padding;
	    //protected AlgoMode cudnnAlgoMode = AlgoMode.PREFER_FASTEST;
	    //protected FwdAlgo cudnnFwdAlgo;
	    //protected BwdFilterAlgo cudnnBwdFilterAlgo;
	    //protected BwdDataAlgo cudnnBwdDataAlgo;
	    
	}
	
    Configuration conf;

    public DQN buildDQN(int[] shapeInputs, int numOutputs) {
    		if (shapeInputs.length<3) {
    			throw new IllegalArgumentException("shapeInputs must have three elements: depth, height, width");
    		}
    		return buildDQN(shapeInputs[1], shapeInputs[2], shapeInputs[0], numOutputs);
    }
    
    public DQN buildDQN(int height, int width, int depth, int numOutputs) {

        NeuralNetConfiguration.ListBuilder confB = new NeuralNetConfiguration.Builder().seed(Constants.NEURAL_NET_SEED)
                        .iterations(1).optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .learningRate(conf.getLearningRate())
                        .regularization(conf.getL2() > 0)
                        .l2(conf.getL2())
                        //.updater(Updater.NESTEROVS).momentum(0.9)
                        //.updater(Updater.RMSPROP).rmsDecay(conf.getRmsDecay())
                        .updater(conf.getUpdater() != null ? conf.getUpdater() : new Adam())
                        .weightInit(WeightInit.XAVIER).regularization(true).l2(conf.getL2()).list()
                        .layer(0, new ConvolutionLayer.Builder(4, 4).nIn(depth).nOut(32)
                        					.stride(1, 1).padding(3,3)
                                        .activation(Activation.RELU).build());

        //confB.layer(1, new ConvolutionLayer.Builder(3, 3).nOut(32).stride(1, 1).activation(Activation.RELU).build());

        confB.layer(1, new DenseLayer.Builder().nOut(8).activation(Activation.RELU).build());

        confB.layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY).nOut(numOutputs)
                        .build());

        confB.setInputType(InputType.convolutional(height, width, depth));
        MultiLayerConfiguration mlnconf = confB.pretrain(false).backprop(true).build();
        MultiLayerNetwork model = new MultiLayerNetwork(mlnconf);
        model.init();
        if (conf.getListeners() != null) {
            model.setListeners(conf.getListeners());
        } else {
            model.setListeners(new ScoreIterationListener(Constants.NEURAL_NET_ITERATION_LISTENER));
        }

        return new DQN(model);
    }

    @AllArgsConstructor
    @Builder
    @Value
    public static class Configuration {

        double learningRate;
        double l2;
        IUpdater updater;
        IterationListener[] listeners;
    }

}
