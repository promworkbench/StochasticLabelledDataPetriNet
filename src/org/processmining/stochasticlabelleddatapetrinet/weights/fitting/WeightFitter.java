package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import org.deckfour.xes.model.XLog;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;

public interface WeightFitter<T extends StochasticLabelledDataPetriNetWeights<T>> {

	StochasticLabelledDataPetriNetWeights<T> fit(XLog log, StochasticLabelledDataPetriNet<?> sldpn)
			throws WeightFitterException;

}
