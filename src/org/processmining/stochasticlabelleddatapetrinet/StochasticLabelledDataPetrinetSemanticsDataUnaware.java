package org.processmining.stochasticlabelleddatapetrinet;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataStateFactoryImpl;

public class StochasticLabelledDataPetrinetSemanticsDataUnaware extends StochasticLabelledDataPetriNetSemanticsImpl {
	public StochasticLabelledDataPetrinetSemanticsDataUnaware(StochasticLabelledDataPetriNet<?> net) {
		super(net, new DataStateFactoryImpl(0));
		setStrictWriteSemantics(false); // we do not care about writing
	}

	public double getTransitionWeight(int transition) {
		return 0;
	}

	public double getTotalWeightOfEnabledTransitions() {
		return 0;
	}
}