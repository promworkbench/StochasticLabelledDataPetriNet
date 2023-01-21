package org.processmining.stochasticlabelleddatapetrinet;

import java.util.Collections;
import java.util.List;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataStateFactoryImpl;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

public class StochasticLabelledDataPetriNetWeightsDataIndependent extends StochasticLabelledDataPetriNetImpl
		implements StochasticLabelledDataPetriNetWeights {

	public StochasticLabelledDataPetriNetWeightsDataIndependent(StochasticLabelledPetriNet slpn) {
		super(slpn, List.of(), List.of(), 
			  emptyListOfLists(slpn.getNumberOfTransitions()), emptyListOfLists(slpn.getNumberOfTransitions()));
	}

	private static List<int[]> emptyListOfLists(int size) {
		return Collections.nCopies(size, new int[] {});
	}

	@Override
	public double getTransitionWeight(int transition, DataState dataState) {
		return 1.0;
	}

	@Override
	public StochasticLabelledDataPetriNetSemantics getDefaultSemantics() {
		return new StochasticLabelledDataPetriNetSemanticsWeightsImpl(this,
				new DataStateFactoryImpl(getNumberOfVariables()));
	}

}