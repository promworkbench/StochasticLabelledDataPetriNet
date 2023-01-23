package org.processmining.stochasticlabelleddatapetrinet;

import java.util.ArrayList;
import java.util.List;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataStateFactoryImpl;
import org.processmining.stochasticlabelleddatapetrinet.io.StochasticLabelledDataPetriNetSerializerImpl;
import org.processmining.stochasticlabelleddatapetrinet.weights.ConstantWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.WeightFunction;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

public class StochasticLabelledDataPetriNetWeightsDataDependent
		extends StochasticLabelledDataPetriNetImpl<StochasticLabelledDataPetriNetWeightsDataDependent>
		implements StochasticLabelledDataPetriNetWeights<StochasticLabelledDataPetriNetWeightsDataDependent> {

	private final List<WeightFunction> transitionWeights = new ArrayList<>();

	/**
	 * Copy from existing SLDPN
	 * 
	 * @param sldpn
	 */
	public StochasticLabelledDataPetriNetWeightsDataDependent(StochasticLabelledDataPetriNet<?> sldpn) {
		super(sldpn);
		for (int i = 0; i < getNumberOfTransitions(); i++) {
			transitionWeights.add(new ConstantWeightFunction());
		}
	}

	/**
	 * Copy from existing SLDPN with changed data perspective
	 * 
	 * @param sldpn
	 * @param variableLabels
	 * @param variableTypes
	 * @param transitionReadVariables
	 * @param transitionWriteVariables
	 */
	public StochasticLabelledDataPetriNetWeightsDataDependent(StochasticLabelledDataPetriNet<?> sldpn,
			List<String> variableLabels, List<VariableType> variableTypes, List<int[]> transitionReadVariables,
			List<int[]> transitionWriteVariables) {
		super(new SLPN() {

			public int isInInitialMarking(int place) {
				return sldpn.isInInitialMarking(place);
			}

			public String getTransitionLabel(int transition) {
				return sldpn.getTransitionLabel(transition);
			}

			public int[] getOutputTransitions(int place) {
				return sldpn.getOutputTransitions(place);
			}

			public int[] getOutputPlaces(int transition) {
				return sldpn.getOutputPlaces(transition);
			}

			public int getNumberOfTransitions() {
				return sldpn.getNumberOfTransitions();
			}

			public int getNumberOfPlaces() {
				return sldpn.getNumberOfPlaces();
			}

			public int[] getInputTransitions(int place) {
				return sldpn.getInputTransitions(place);
			}

			public int[] getInputPlaces(int transition) {
				return sldpn.getInputPlaces(transition);
			}
		}, variableLabels, variableTypes, transitionReadVariables, transitionWriteVariables);
		for (int i = 0; i < getNumberOfTransitions(); i++) {
			transitionWeights.add(new ConstantWeightFunction());
		}
	}

	/**
	 * 
	 * Copy from existing SLPN with added data perspective
	 * 
	 * @param slpn
	 * @param variableLabels
	 * @param variableTypes
	 * @param transitionReadVariables
	 * @param transitionWriteVariables
	 */
	public StochasticLabelledDataPetriNetWeightsDataDependent(StochasticLabelledPetriNet slpn,
			List<String> variableLabels, List<VariableType> variableTypes, List<int[]> transitionReadVariables,
			List<int[]> transitionWriteVariables) {
		super(slpn, variableLabels, variableTypes, transitionReadVariables, transitionWriteVariables);
		for (int i = 0; i < getNumberOfTransitions(); i++) {
			transitionWeights.add(new ConstantWeightFunction());
		}
	}

	public void setWeightFunction(int transition, WeightFunction function) {
		transitionWeights.set(transition, function);
	}

	public WeightFunction getWeightFunction(int transition) {
		return transitionWeights.get(transition);
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

	public StochasticLabelledDataPetriNetSerializerImpl getDefaultSerializer() {
		return new StochasticLabelledDataPetriNetSerializerImpl();
	}

}