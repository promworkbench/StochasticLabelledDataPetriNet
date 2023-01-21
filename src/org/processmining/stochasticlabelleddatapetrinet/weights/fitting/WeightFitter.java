package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import org.deckfour.xes.model.XLog;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;

public interface WeightFitter {

	StochasticLabelledDataPetriNetWeights fit(XLog log, StochasticLabelledDataPetriNet sldpn)
			throws WeightFitterException;

}
