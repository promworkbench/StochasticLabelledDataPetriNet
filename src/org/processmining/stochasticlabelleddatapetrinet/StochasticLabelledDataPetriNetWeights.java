package org.processmining.stochasticlabelleddatapetrinet;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public interface StochasticLabelledDataPetriNetWeights extends StochasticLabelledDataPetriNet {

	/**
	 * 
	 * @param transition
	 * @param dataState
	 * @return the weight of the transition.
	 */
	public double getTransitionWeight(int transition, DataState dataState);

}