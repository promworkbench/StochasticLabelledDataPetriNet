package org.processmining.stochasticlabelleddatapetrinet;

import java.util.ArrayList;
import java.util.List;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataStateFactoryImpl;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

import weights.ConstantWeightFunction;
import weights.WeightFunction;

public class StochasticLabelledDataPetriNetWeightsDataDependentImpl extends StochasticLabelledDataPetriNetImpl
		implements StochasticLabelledDataPetriNetWeights {

	private List<WeightFunction> transitionWeights = new ArrayList<>();
	
	/**
	 * 
	 * @param slpn
	 * @param variableLabels
	 * @param variableTypes
	 * @param transitionReadVariables
	 * @param transitionWriteVariables
	 */
	public StochasticLabelledDataPetriNetWeightsDataDependentImpl(StochasticLabelledPetriNet slpn,
			List<String> variableLabels, List<VariableType> variableTypes, List<int[]> transitionReadVariables,
			List<int[]> transitionWriteVariables) {
		super(slpn, variableLabels, variableTypes, transitionReadVariables, transitionWriteVariables);
		for (int i = 0; i < getNumberOfTransitions(); i++) {
			transitionWeights.add(new ConstantWeightFunction());
		}
	}

	public void setWeightFunction(int transitionidx, WeightFunction function) {
		transitionWeights.set(transitionidx, function);
	}	

	@Override
	public double getTransitionWeight(int transition, DataState dataState) {
		return transitionWeights.get(transition).evaluateWeight(dataState);
	}

	@Override
	public StochasticLabelledDataPetriNetSemantics getDefaultSemantics() {
		return new StochasticLabelledDataPetriNetSemanticsWeightsImpl(this,
				new DataStateFactoryImpl(getNumberOfVariables())); 
	}

}