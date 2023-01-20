package org.processmining.stochasticlabelleddatapetrinet.probability;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class FollowerSemanticsDataImpl implements FollowerSemanticsData<Integer, DataState> {

	private final String[] activityTrace;
	private final DataState[] dataTrace;

	public FollowerSemanticsDataImpl(String[] activityTrace, DataState[] dataTrace) {
		assert dataTrace.length > 0; //there is always at least one data state: the initial one

		this.activityTrace = activityTrace;
		this.dataTrace = dataTrace;
	}

	public Pair<Integer, DataState> getInitialState() {
		return Pair.of(0, dataTrace[0]);
	}

	public Integer takeStep(Integer state, String label) {
		if (activityTrace[state].equals(label)) {
			return state+1;
		} else {
			return null;
		}
	}

	public boolean isFinalState(Integer state) {
		return state == activityTrace.length;
	}

	public DataState getDataStateAfter(Integer state) {
		if (state + 1 < dataTrace.length) {
			return dataTrace[state + 1];
		} else {
			return dataTrace[dataTrace.length - 1];
		}
	}

}