package org.processmining.stochasticlabelleddatapetrinets.weights.fitting;

import static org.junit.Assert.assertNotNull;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.log.utils.XUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.LogisticRegressionWeightFitter;
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
	public void weightFittingSingleVariableTest() throws UnknownTreeNodeException, ReductionFailedException, Exception {

		StochasticLabelledDataPetriNet net = SepsisTestLog.buildSepsisBaseModel();
		XLog log = SepsisTestLog.loadSepsisLog();
		
		System.out.println(XUtils.getEventAttributeKeys(log));
		
		
		
		LogisticRegressionWeightFitter fitter = new LogisticRegressionWeightFitter(new XEventNameClassifier());
		
		StochasticLabelledDataPetriNetWeights netWithWeights = fitter.fit(log, net);
		
		assertNotNull(netWithWeights);
	}
	
}
