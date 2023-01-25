package org.processmining.stochasticlabelleddatapetrinets.weights.fitting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.LogisticRegressionWeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.WeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.WeightFitterException;
import org.processmining.stochasticlabelleddatapetrinet.weights.writeops.AllWriteOperationMiner;
import org.processmining.stochasticlabelleddatapetrinets.IntegrationTestUtil;
import org.processmining.stochasticlabelleddatapetrinets.SimpleTestLog;

public class LogisticRegressionWeightFitterTest {
	
	@BeforeClass
	public static void init() throws Throwable {
		try {
			IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
		} catch (Throwable e) {
			// Ignore we only load LpSolve
		}
	}
	
	@Test
	public void weightFittingSingleVariableTest() throws WeightFitterException {
		
		StochasticLabelledDataPetriNet net = SimpleTestLog.buildDataWeightTestModel();
		XLog log = SimpleTestLog.buildTestLog();
		
		WeightFitter fitter = new LogisticRegressionWeightFitter(new XEventNameClassifier());
		
		StochasticLabelledDataPetriNetWeights netWithWeights = fitter.fit(log, net);
		
		assertNotNull(netWithWeights);
	}
		
	@Test
	public void weightFittingTwoVariablesTest() throws WeightFitterException {
		
		StochasticLabelledDataPetriNet net = SimpleTestLog.buildDataWeight2VariablesTestModel();
		XLog log = SimpleTestLog.buildTestLog2Variables();
		
		WeightFitter fitter = new LogisticRegressionWeightFitter(new XEventNameClassifier());
		
		StochasticLabelledDataPetriNetWeights netWithWeights = fitter.fit(log, net);
		
		assertNotNull(netWithWeights);
		
		// First transition should not have a logistic weight function attached
		assertEquals(1.0, netWithWeights.getTransitionWeight(0, netWithWeights.getDefaultSemantics().newDataState()), 0.01);
		
		// A should have high weight for values 10 and -10
		DataState ds = netWithWeights.getDefaultSemantics().newDataState();
		
		ds.putDouble(0, 10); // variable X
		ds.putDouble(1, -10); // variable Y
		System.out.println(netWithWeights.getTransitionWeight(1, ds));
		assertEquals(0.9999999999999976, netWithWeights.getTransitionWeight(1, ds), 0.00001);
		ds.clear();

		ds.putDouble(0, 5); // variable X
		ds.putDouble(1, -5); // variable Y
		System.out.println(netWithWeights.getTransitionWeight(1, ds));
		assertEquals(2.705002102531481E-11, netWithWeights.getTransitionWeight(1, ds), 0.00001);
		ds.clear();

		ds.putDouble(0, 10); // variable X
		ds.putDouble(1, -10); // variable Y
		System.out.println(netWithWeights.getTransitionWeight(1, ds));
		assertEquals(2.39195190251976E-15, netWithWeights.getTransitionWeight(2, ds), 0.00001);
		ds.clear();		
		
		ds.putDouble(0, 5); // variable X
		ds.putDouble(1, -5); // variable Y
		System.out.println(netWithWeights.getTransitionWeight(1, ds));
		assertEquals(0.9999999999729501, netWithWeights.getTransitionWeight(2, ds), 0.00001);
		ds.clear();
		
	}
	
	@Test
	public void weightFittingCaseVariablesTest() throws WeightFitterException {
		
		StochasticLabelledDataPetriNet net = SimpleTestLog.buildConstantWeightTestModel();
		XLog log = SimpleTestLog.buildTestCaseVarsLog();
		
		AllWriteOperationMiner writeOpMiner = new AllWriteOperationMiner(log);
		
		net = writeOpMiner.extendWithWrites(net);
		
		WeightFitter fitter = new LogisticRegressionWeightFitter(new XEventNameClassifier());
		
		StochasticLabelledDataPetriNetWeights netWithWeights = fitter.fit(log, net);
		
		assertNotNull(netWithWeights);
		
		// First transition should not have a logistic weight function attached
		assertEquals(1.0, netWithWeights.getTransitionWeight(0, netWithWeights.getDefaultSemantics().newDataState()), 0.01);
		
		// A should have high weight for values 10 and -10
		DataState ds = netWithWeights.getDefaultSemantics().newDataState();
		
		ds.putDouble(0, 10); // variable X
		System.out.println(netWithWeights.getTransitionWeight(1, ds));
		assertEquals(0.9999999999999976, netWithWeights.getTransitionWeight(1, ds), 0.00001);
		ds.clear();

		ds.putDouble(0, 5); // variable X
		System.out.println(netWithWeights.getTransitionWeight(1, ds));
		assertEquals(2.705002102531481E-11, netWithWeights.getTransitionWeight(1, ds), 0.00001);
		ds.clear();

		ds.putDouble(0, 10); // variable X
		System.out.println(netWithWeights.getTransitionWeight(1, ds));
		assertEquals(2.39195190251976E-15, netWithWeights.getTransitionWeight(2, ds), 0.00001);
		ds.clear();		
		
		ds.putDouble(0, 5); // variable X
		System.out.println(netWithWeights.getTransitionWeight(1, ds));
		assertEquals(0.9999999999729501, netWithWeights.getTransitionWeight(2, ds), 0.00001);
		ds.clear();
		
	}

	@Test
	public void weightFittingABCaseVariablesTest() throws WeightFitterException {
		
		StochasticLabelledDataPetriNet net = SimpleTestLog.buildABModel();
		XLog log = SimpleTestLog.buildTestLog2TraceVariables();
		
		AllWriteOperationMiner writeOpMiner = new AllWriteOperationMiner(log);
		
		net = writeOpMiner.extendWithWrites(net);
		
		WeightFitter fitter = new LogisticRegressionWeightFitter(new XEventNameClassifier());
		
		StochasticLabelledDataPetriNetWeights netWithWeights = fitter.fit(log, net);
		
		assertNotNull(netWithWeights);
		
		// A should have high weight for values 10 and -10
		DataState ds = netWithWeights.getDefaultSemantics().newDataState();
		
		ds.putLong(0, 10); // variable X
		System.out.println(netWithWeights.getTransitionWeight(0, ds)); // transition A
		assertEquals(1.0, netWithWeights.getTransitionWeight(0, ds), 0.00001);
		ds.clear();
		
		ds.putLong(0, 5); // variable X
		System.out.println(netWithWeights.getTransitionWeight(1, ds)); // transition B
		assertEquals(1.0, netWithWeights.getTransitionWeight(1, ds), 0.00001);
		ds.clear();
		
		ds.putLong(0, 10); // variable X
		System.out.println(netWithWeights.getTransitionWeight(1, ds)); // transition B
		assertEquals(0.0, netWithWeights.getTransitionWeight(1, ds), 0.00001);
		ds.clear();
		
	}

	
}
