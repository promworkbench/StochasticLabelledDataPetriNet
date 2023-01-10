package org.processmining.stochasticlabelleddatapetrinet;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataStateFactory;

public class StochasticLabelledDataPetriNetSemanticsWeightsImpl extends StochasticLabelledDataPetriNetSemanticsImpl {

	private StochasticLabelledDataPetriNetWeights net;

	public StochasticLabelledDataPetriNetSemanticsWeightsImpl(StochasticLabelledDataPetriNetWeights net, DataStateFactory dataStateFactory) {
		super(net, dataStateFactory);
		this.net = net;
	}

	@Override
	public double getTransitionWeight(int transition) {
		return net.getTransitionWeight(transition, getDataState());
	}

	@Override
	public double getTotalWeightOfEnabledTransitions() {
		double result = 0;
		for (int transition = enabledTransitions.nextSetBit(0); transition >= 0; transition = enabledTransitions
				.nextSetBit(transition + 1)) {
			result += net.getTransitionWeight(transition, getDataState());
		}
		return result;
	}

	@Override
	public StochasticLabelledDataPetriNetSemanticsWeightsImpl clone() {
		StochasticLabelledDataPetriNetSemanticsWeightsImpl result = (StochasticLabelledDataPetriNetSemanticsWeightsImpl) super.clone();
		result.net = net;
		return result;
	}

}