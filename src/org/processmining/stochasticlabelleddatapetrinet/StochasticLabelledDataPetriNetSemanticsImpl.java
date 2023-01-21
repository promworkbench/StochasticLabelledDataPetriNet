package org.processmining.stochasticlabelleddatapetrinet;

import java.util.BitSet;

import org.processmining.datapetrinets.exception.VariableNotFoundException;
import org.processmining.datapetrinets.expression.VariableProvider;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataStateFactory;
import org.python.bouncycastle.util.Arrays;

public abstract class StochasticLabelledDataPetriNetSemanticsImpl implements StochasticLabelledDataPetriNetSemantics {

	private StochasticLabelledDataPetriNet net;
	private DataStateFactory dataStateFactory;

	private byte[] state;
	private byte[] cacheState;

	private DataState dataState;
	private boolean isStrictWriteSemantics;
	
	private BitSet cacheTransition;	
	protected BitSet enabledTransitions;
	protected int numberOfEnabledTransitions;

	public StochasticLabelledDataPetriNetSemanticsImpl(StochasticLabelledDataPetriNet net,
			DataStateFactory dataStateFactory) {
		this.net = net;
		this.dataStateFactory = dataStateFactory;
		this.state = new byte[net.getNumberOfPlaces()];
		this.dataState = dataStateFactory.newDataState();
		this.cacheState = new byte[net.getNumberOfPlaces()];
		this.cacheTransition = new BitSet(net.getNumberOfTransitions());
		this.enabledTransitions = new BitSet(net.getNumberOfTransitions());
		// Default data state is empty (all variables are not set)
		setInitialState(dataState);
	}
	
	@Override
	public void setInitialState(DataState initialDataState) {
		enabledTransitions.clear();
		numberOfEnabledTransitions = 0;
		for (int place = 0; place < net.getNumberOfPlaces(); place++) {
			state[place] = (byte) net.isInInitialMarking(place);
		}
		dataState = initialDataState.deepCopy();
		computeEnabledTransitions();		
	}	

	@Override
	public void executeTransition(int transition, DataState dataEffect) {
				
		// Fire transition 
		
		int[] inSet = net.getInputPlaces(transition);
		for (int place : inSet) {
			if (state[place] == 0) {
				throw new RuntimeException("non-existing token consumed");
			}
			state[place]--;

			//update the enabled transitions; some transitions might be disabled by this execution
			for (int transitionT : net.getOutputTransitions(place)) {
				computeEnabledTransition(transitionT);
			}
		}

		int[] postSet = net.getOutputPlaces(transition);
		for (int place : postSet) {
			if (state[place] == Byte.MAX_VALUE) {
				throw new RuntimeException("maximum number of tokens in a place exceeded");
			}
			state[place]++;

			//update the enabled transitions; some transitions might be enabled by this execution
			for (int transitionT : net.getOutputTransitions(place)) {
				computeEnabledTransition(transitionT);
			}
		}
		
		
		// Check values
		if (isStrictWriteSemantics()) {
			int[] writeVariables = net.getWriteVariables(transition);
			for (int i = 0; i < writeVariables.length; i++) {
				int varIdx = writeVariables[i];
				if (!dataEffect.contains(varIdx)) {
					throw new RuntimeException("Cannot execute transition, write variable "+net.getVariableLabel(varIdx)+" is missing in dataEffect!");
				}
			}
		}
		
		// Update data state
		dataState.update(dataEffect);	
	}

	private boolean computeEnabledTransition(int transition) {
		//due to potential multiplicity of arcs, we have to keep track of how many tokens we would consume
		System.arraycopy(state, 0, cacheState, 0, state.length);

		int[] inSet = net.getInputPlaces(transition);
		for (int inPlace : inSet) {
			if (cacheState[inPlace] == 0) {
				if (enabledTransitions.get(transition)) {
					enabledTransitions.set(transition, false);
					numberOfEnabledTransitions--;
				}
				return false;
			} else {
				cacheState[inPlace]--;
			}
		}

		if (!enabledTransitions.get(transition)) {
			enabledTransitions.set(transition, true);
			numberOfEnabledTransitions++;
		}
		return true;
	}

	private void computeEnabledTransitions() {
		numberOfEnabledTransitions = 0;
		for (int transition = 0; transition < net.getNumberOfTransitions(); transition++) {
			computeEnabledTransition(transition);
		}
	}

	@Override
	public BitSet getEnabledTransitions() {
		
		// TODO filter the enabled set by checking guard enablement 
		// this should be probably done only after executing transitions since data state should otherwise not change

		for (int i = enabledTransitions.nextSetBit(0); i >= 0; i = enabledTransitions.nextSetBit(i + 1)) {
			// check if guard is violated
			boolean violated = net.getGuardExpression(i).isFalse(new VariableProvider() {
				
				public Object getValue(String variableName) throws VariableNotFoundException {
					//TODO probably keeping a variable/index map somewhere would be good but ignoring for now since number of variables will be small
					for (int j = 0; j < net.getNumberOfVariables(); j++) {
						if (net.getVariableLabel(j).equals(variableName)) {
							if (net.getVariableType(j) == VariableType.CONTINUOUS) {
								return dataState.tryGetDouble(j);
							} else if (net.getVariableType(j) == VariableType.DISCRETE) {
								return dataState.tryGetLong(j);
							} else {
								return dataState.getLong(j); // TODO categorical as String needs to be implemented
							}							
						}
					}
					return null;
				}
			});
			
			if (violated) {
				enabledTransitions.clear(i);
			}
			
			if (i == Integer.MAX_VALUE) {
				break; // or (i+1) would overflow
			}					
		}

		return (BitSet) enabledTransitions.clone();
	}

	@Override
	public boolean isFinalState() {
		return numberOfEnabledTransitions == 0;
	}
	

	@Override
	public byte[] getState() {
		return Arrays.clone(state);
	}

	@Override
	public void setState(byte[] newState) {
		byte[] oldState = this.state;
		this.state = Arrays.clone(newState);

		cacheTransition.clear();

		//walk through all places that have changed, and update the transition enabledness
		for (int place = 0; place < net.getNumberOfPlaces(); place++) {
			if (oldState[place] != state[place]) {
				for (int transition : net.getInputTransitions(place)) {
					if (!cacheTransition.get(transition)) {
						computeEnabledTransition(transition);
						cacheTransition.set(transition);
					}
				}
				for (int transition : net.getOutputTransitions(place)) {
					if (!cacheTransition.get(transition)) {
						computeEnabledTransition(transition);
						cacheTransition.set(transition);
					}
				}
			}
		}
	}

	public DataState getDataState() {
		return dataState.deepCopy();
	}

	public void setDataState(DataState dataState) {
		this.dataState = dataState.deepCopy();
	}
	
	public DataState newDataState() {
		return dataStateFactory.newDataState();
	}
	
	@Override
	public boolean isTransitionSilent(int transition) {
		return net.isTransitionSilent(transition);
	}
	

	@Override
	public String getTransitionLabel(int transition) {
		return net.getTransitionLabel(transition);
	}	
	
	public int getNumberOfVariables() {
		return net.getNumberOfVariables();
	}

	public String getVariableLabel(int variable) {
		return net.getVariableLabel(variable);
	}

	public VariableType getVariableType(int variable) {
		return net.getVariableType(variable);
	}
	

	@Override
	public StochasticLabelledDataPetriNetSemanticsImpl clone() {
		StochasticLabelledDataPetriNetSemanticsImpl result;
		try {
			result = (StochasticLabelledDataPetriNetSemanticsImpl) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported!");
		}

		result.net = net;
		result.dataStateFactory = dataStateFactory;
		result.cacheState = Arrays.clone(cacheState);
		result.cacheTransition = (BitSet) cacheTransition.clone();
		result.enabledTransitions = (BitSet) enabledTransitions.clone();
		result.numberOfEnabledTransitions = numberOfEnabledTransitions;
		result.state = Arrays.clone(state);
		result.dataState = dataState.deepCopy();

		return result;
	}

	public boolean isStrictWriteSemantics() {
		return isStrictWriteSemantics;
	}

	public void setStrictWriteSemantics(boolean isStrictWriteSemantics) {
		this.isStrictWriteSemantics = isStrictWriteSemantics;
	}

}