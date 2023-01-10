package org.processmining.stochasticlabelleddatapetrinet;

import java.util.ArrayList;
import java.util.List;

import org.processmining.datapetrinets.expression.GuardExpression;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNet;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public abstract class StochasticLabelledDataPetriNetImpl implements StochasticLabelledDataPetriNet {

	private final List<String> transitionLabels = new ArrayList<>();
	private final List<String> variableLabels;
	private final List<VariableType> variableTypes;

	private final TIntIntMap initialMarking = new TIntIntHashMap(10, 0.5f, -1, 0);

	private final List<int[]> inputPlaces = new ArrayList<>();
	private final List<int[]> outputPlaces = new ArrayList<>();
	private final List<int[]> inputTransitions = new ArrayList<>();
	private final List<int[]> outputTransitions = new ArrayList<>();

	private final List<int[]> readVariables;
	private final List<int[]> writeVariables;

	/**
	 * Copy constructor from an existing SLPN and the given data elements providing an immutable SLDPN.
	 * 
	 * @param slpn
	 * @param variableLabels
	 * @param variableTypes
	 * @param transitionReadVariables
	 * @param transitionWriteVariables
	 */
	public StochasticLabelledDataPetriNetImpl(StochasticLabelledPetriNet slpn, 
											  List<String> variableLabels,
											  List<VariableType> variableTypes, 
											  List<int[]> transitionReadVariables,
											  List<int[]> transitionWriteVariables) {
		
		this.readVariables = transitionReadVariables;
		this.writeVariables = transitionWriteVariables;
		this.variableLabels = variableLabels;
		this.variableTypes = variableTypes;

		for (int i = 0; i < slpn.getNumberOfTransitions(); i++) {
			transitionLabels.add(slpn.getTransitionLabel(i));
		}
		
		for (int i = 0; i < slpn.getNumberOfTransitions(); i++) {
			inputPlaces.add(slpn.getInputPlaces(i));
			outputPlaces.add(slpn.getOutputPlaces(i));			
		}
		
		for (int i = 0; i < slpn.getNumberOfPlaces(); i++) {
			inputTransitions.add(slpn.getInputTransitions(i));
			outputTransitions.add(slpn.getOutputTransitions(i));			
		}
		
		for (int i = 0; i < slpn.getNumberOfPlaces(); i++) {
			if (slpn.isInInitialMarking(i) > 0) {
				initialMarking.put(i, slpn.isInInitialMarking(i));
			}
		}		
		
	}

	@Override
	public int getNumberOfTransitions() {
		return transitionLabels.size();
	}

	@Override
	public int getNumberOfPlaces() {
		return inputTransitions.size();
	}

	@Override
	public String getTransitionLabel(int transition) {
		return transitionLabels.get(transition);
	}

	@Override
	public boolean isTransitionSilent(int transition) {
		return transitionLabels.get(transition) == null;
	}

	@Override
	public int isInInitialMarking(int place) {
		return initialMarking.get(place);
	}

	@Override
	public int[] getInputPlaces(int transition) {
		return inputPlaces.get(transition);
	}

	@Override
	public int[] getOutputPlaces(int transition) {
		return outputPlaces.get(transition);
	}

	@Override
	public int[] getInputTransitions(int place) {
		return inputTransitions.get(place);
	}

	@Override
	public int[] getOutputTransitions(int place) {
		return outputTransitions.get(place);
	}

	public int getNumberOfVariables() {
		return variableLabels.size();
	}

	public String getVariableLabel(int variable) {
		return variableLabels.get(variable);
	}

	public VariableType getVariableType(int variable) {
		return variableTypes.get(variable);
	}

	public int[] getReadVariables(int transition) {
		return readVariables.get(transition);
	}

	public int[] getWriteVariables(int transition) {
		return writeVariables.get(transition);
	}

	public GuardExpression getGuardExpression(int transition) {
		return GuardExpression.Factory.trueInstance(); // Not guarded as standard
	}

}