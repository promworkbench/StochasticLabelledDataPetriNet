package org.processmining.stochasticlabelleddatapetrinets.weights.fitting;

import static org.junit.Assert.assertNotNull;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.LogisticRegressionWeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.WeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.writeops.AllWriteOperationMiner;
import org.processmining.stochasticlabelleddatapetrinets.IntegrationTestUtil;
import org.processmining.stochasticlabelleddatapetrinets.SepsisTestLog;

public class LogisticRegressionWeightFitterSepsisLogTest {
	
	@BeforeClass
	public static void init() throws Throwable {
		try {
			IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
		} catch (Throwable e) {
			// Ignore we only load LpSolve
		}
	}
	
	@Test
	public void weightFittingSepsisTest() throws UnknownTreeNodeException, ReductionFailedException, Exception {

		StochasticLabelledDataPetriNet net = SepsisTestLog.buildSepsisBaseModel();
		XLog log = SepsisTestLog.loadSepsisLog();
		
		OneHotEncoding oneHotEncoding = new OneHotEncoding(10);
		oneHotEncoding.fit(log);
		
		log = oneHotEncoding.process(log);
		
		AllWriteOperationMiner writeOpMiner = new AllWriteOperationMiner(log);
		
		net = writeOpMiner.extendWithWrites(net);
		
		WeightFitter fitter = new LogisticRegressionWeightFitter(new XEventNameClassifier());
		
		StochasticLabelledDataPetriNetWeights netWithWeights = fitter.fit(log, net);
		
		assertNotNull(netWithWeights);
	}
	
}
