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

public class LogisticRegressionWeightFitterBPI2020LogTest {
	
	@BeforeClass
	public static void init() throws Throwable {
		try {
			IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
		} catch (Throwable e) {
			// Ignore we only load LpSolve
		}
	}
	
	@Test
	public void weightFittingBPI2020Test() throws UnknownTreeNodeException, ReductionFailedException, Exception {
	
		try (InputStream bpi20Model = getClass().getResourceAsStream("bpic2020-DomesticDeclarations.xes.gz-IMf.apnml");
				 InputStream bpi20Log = getClass().getResourceAsStream("bpic2020-DomesticDeclarations.xes.gz6.xes.gz")) {
			
			AcceptingPetriNet pn = AcceptingPetriNetFactory.createAcceptingPetriNet();
			pn.importFromStream(new FakeContext(), bpi20Model);
			
			XLog log = XUtils.loadLog(new GZIPInputStream(bpi20Log));
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
