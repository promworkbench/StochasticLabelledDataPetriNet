package org.processmining.stochasticlabelleddatapetrinet.weights;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public interface WeightFunction {

	double evaluateWeight(StochasticLabelledDataPetriNet net, DataState dataState);		

}