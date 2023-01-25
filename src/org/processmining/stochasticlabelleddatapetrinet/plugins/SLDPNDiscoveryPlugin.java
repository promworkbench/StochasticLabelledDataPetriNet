package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.LogisticRegressionWeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.WeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.WeightFitterException;
import org.processmining.stochasticlabelleddatapetrinet.weights.writeops.AllWriteOperationMiner;

public class SLDPNDiscoveryPlugin {

	public static SLDPN discover(XLog log, AcceptingPetriNet controlFlowModel, XEventClassifier classifier)
			throws WeightFitterException {

		StochasticLabelledDataPetriNet dnet = PetrinetConverter.viewAsSLDPN(controlFlowModel);

		OneHotEncoding OneHotEncoding = new OneHotEncoding();
		log = OneHotEncoding.process(log);

		AllWriteOperationMiner writeOpMiner = new AllWriteOperationMiner(log);

		dnet = writeOpMiner.extendWithWrites(dnet);

		WeightFitter fitter = new LogisticRegressionWeightFitter(classifier);

		StochasticLabelledDataPetriNetWeights netWithWeights = fitter.fit(log, dnet);

		return new SLDPN(OneHotEncoding, netWithWeights);
	}
}