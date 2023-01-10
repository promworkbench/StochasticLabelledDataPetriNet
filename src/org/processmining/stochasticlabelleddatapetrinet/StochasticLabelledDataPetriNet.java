package org.processmining.stochasticlabelleddatapetrinet;

import org.processmining.datapetrinets.expression.GuardExpression;

/**
 * Inspired by StochasticLabelledPetriNet but not in an inheritance relation since structurally different.
 * 
 * @author Felix Mannhardt
 * @author Sander Leemans
 */
public interface StochasticLabelledDataPetriNet {
	
	public enum VariableType {
		CONTINUOUS, DISCRETE, CATEGORICAL 
	}

	/** Control-flow elements **/
	
	/**
	 * 
	 * @return the number of transitions. All transitions have indices starting
	 *         at 0 and ending at the returned value (exclusive).
	 */
	public int getNumberOfTransitions();

	/**
	 * 
	 * @return the number of places. All places have indices starting at 0 and
	 *         ending at the returned value (exclusive).
	 */
	public int getNumberOfPlaces();

	/**
	 * 
	 * @param transition the index of the transition
	 * @return the label of the transition or NULL if there is not label assigned
	 */
	public String getTransitionLabel(int transition);

	/**
	 * 
	 * @param transition the index of the transition
	 * @return whether the transition is a silent transition
	 */
	public boolean isTransitionSilent(int transition);

	/**
	 * 
	 * @param place the index of the place
	 * @return the number of tokens on this place in the initial marking.
	 */
	public int isInInitialMarking(int place);

	/** Data elements **/
		
	public int getNumberOfVariables();
	
	public String getVariableLabel(int variable);
	
	public VariableType getVariableType(int variable);
		
	public int[] getReadVariables(int transition);
	
	public int[] getWriteVariables(int transition);
	
	public GuardExpression getGuardExpression(int transition);
			
	/** Flow relations **/
	
	/**
	 * 
	 * @param transition the index of the transition
	 * @return a list of places that have arcs to this transition. Transitions
	 *         may appear multiple times. The caller must not change the
	 *         returned array.
	 */
	public int[] getInputPlaces(int transition);

	/**
	 * 
	 * @param transition the index of the transition
	 * @return a list of places that have arcs from this transition. Transitions
	 *         may appear multiple times. The caller must not change the
	 *         returned array.
	 */
	public int[] getOutputPlaces(int transition);

	/**
	 * 
	 * @param place the index of the place
	 * @return a list of transitions that have arcs to this place. Places may
	 *         appear multiple times. The caller must not change the returned
	 *         array.
	 */
	public int[] getInputTransitions(int place);

	/**
	 * 
	 * @param place the index of the place
	 * @return a list of transitions that have arcs from this place. Places may
	 *         appear multiple times. The caller must not change the returned
	 *         array.
	 */
	public int[] getOutputTransitions(int place);
	
	
	/** Helper methods **/

	/**
	 * 
	 * @return an object that allows for a standardised interpretation of the
	 *         language of the net. The returned object might not be thread safe
	 *         and the implementer must ensure a new, fresh, object is returned
	 *         for each call.
	 */
	public StochasticLabelledDataPetriNetSemantics getDefaultSemantics();
}
