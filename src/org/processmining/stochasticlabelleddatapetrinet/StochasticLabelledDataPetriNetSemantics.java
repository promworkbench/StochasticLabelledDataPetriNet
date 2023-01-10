package org.processmining.stochasticlabelleddatapetrinet;

import java.util.BitSet;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

/**
 * Semantics considering the DataState as it is updated through visible transitions writing variables.
 * 
 * @author Felix Mannhardt
 * @author Sander Leemans
 *
 */
public interface StochasticLabelledDataPetriNetSemantics extends Cloneable {
	
	/**
	 * (Re)set the semantics to the initial state
	 */
	public void setInitialState(DataState initialDataState);

	/**
	 * Update the state to reflect execution of the transition.
	 * 
	 * @param transition index fired
	 * @param dataEffect containing values for all (and only those) written variables
	 */
	public void executeTransition(int transition, DataState dataEffect);

	/**
	 * 
	 * @param state
	 * @return an array of indices of the transitions that are enabled. May be
	 *         changed and stored by the caller.
	 */
	public BitSet getEnabledTransitions();

	/**
	 * 
	 * @return whether the current state is a final state.
	 */
	public boolean isFinalState();

	/**
	 * 
	 * @return a copy of the current state.
	 */
	public byte[] getState();

	/**
	 * Set a copy of the given state.
	 * 
	 * @param state
	 */
	public void setState(byte[] state);
	
	/**
	 * @return a copy of the current data state
	 */
	public DataState getDataState();
	
	/**
	 * Set the data state to a copy of the supplied state
	 * 
	 * @param dataState
	 */
	public void setDataState(DataState dataState);

	public DataState newDataState();	
			
	/**
	 * @param transition
	 * @return the weight of the transition. This might depend on the state.
	 */
	public double getTransitionWeight(int transition);

	/**
	 * 
	 * @param enabledTransitions
	 * @return the sum of the weight of the enabled transitions
	 */
	public double getTotalWeightOfEnabledTransitions();
	

	
	/** Utilities **/

	public StochasticLabelledDataPetriNetSemantics clone();

	
	/** Methods required from the SLDPN interface **/
	
	/**
	 * @param transition index
	 * @return whether the transition is observable (false) or silent (true)
	 */
	public boolean isTransitionSilent(int transition);
	
	public String getTransitionLabel(int transition);	
	
	public int getNumberOfVariables();
	
	public String getVariableLabel(int variable);

	public VariableType getVariableType(int variable);

}