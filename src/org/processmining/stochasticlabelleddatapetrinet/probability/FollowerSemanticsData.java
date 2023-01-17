package org.processmining.stochasticlabelleddatapetrinet.probability;

import org.processmining.plugins.InductiveMiner.Pair;

public interface FollowerSemanticsData<B, BD> {
	/**
	 * 
	 * @return The initial state.
	 */
	public abstract Pair<B, BD> getInitialState();

	/**
	 * 
	 * @param label
	 * @return The new state, or null if the step cannot be taken.
	 */
	public abstract B takeStep(B state, String label);

	public abstract boolean isFinalState(B state);

	public abstract BD getDataStateAfter(B state);
}