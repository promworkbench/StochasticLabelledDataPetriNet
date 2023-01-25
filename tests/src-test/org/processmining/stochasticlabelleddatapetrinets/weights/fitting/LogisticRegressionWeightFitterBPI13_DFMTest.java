package org.processmining.stochasticlabelleddatapetrinets.weights.fitting;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.log.utils.XUtils;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.LogisticRegressionWeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.WeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.writeops.AllWriteOperationMiner;
import org.processmining.stochasticlabelleddatapetrinets.FakeContext;
import org.processmining.stochasticlabelleddatapetrinets.IntegrationTestUtil;

public class LogisticRegressionWeightFitterBPI13_DFMTest {
	
	@BeforeClass
	public static void init() throws Throwable {
		try {
			IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
		} catch (Throwable e) {
			// Ignore we only load LpSolve
		}
	}
	
	
	@Test
	public void weightFittingRTFMTest() throws UnknownTreeNodeException, ReductionFailedException, Exception {
		
		try (InputStream rtfmModel = getClass().getResourceAsStream("BPI_Challenge_2013_incidents.xes.gz-DFM.apnml");
			 InputStream rtfmLog = getClass().getResourceAsStream("BPI_Challenge_2013_incidents.xes.gz9.xes.gz")) {
		
			AcceptingPetriNet pn = AcceptingPetriNetFactory.createAcceptingPetriNet();
			pn.importFromStream(new FakeContext(), rtfmModel);
			
			XLog log = XUtils.loadLog(new GZIPInputStream(rtfmLog));
			StochasticLabelledDataPetriNet net = PetrinetConverter.viewAsSLDPN(pn);

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
	
}
