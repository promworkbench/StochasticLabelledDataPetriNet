package org.processmining.stochasticlabelleddatapetrinet.probability;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XTrace;
import org.jbpt.pm.data.DataState;
import org.processmining.plugins.InductiveMiner.Pair;

public class FollowerSemanticsDataImpl implements FollowerSemanticsData<Integer, DataState> {

	private final XTrace trace;
	private final XEventClassifier classifier;

	public FollowerSemanticsDataImpl(XTrace trace, XEventClassifier classifier) {
		this.trace = trace;
		this.classifier = classifier;
	}

	public Pair<Integer, DataState> getInitialState() {
		DataState dataState = new DataStateFactoryImpl().newDataState();
		Log2DataState.traceData2DataState(dataState, trace);
		return Pair.of(0, dataState);
	}

	public Integer takeStep(Integer state, String label) {
		state++;
		if (classifier.getClassIdentity(trace.get(state)).equals(label)) {
			return state;
		} else {
			return null;
		}
	}

	public boolean isFinalState(Integer state) {
		return state < trace.size();
	}

	public DataState getNextDataState(Integer stateB, DataState dataStateB, int transition) {
		DataState result = dataStateB.deepCopy();
		Log2DataState.eventData2DataState(result, trace.get(stateB));
		return result;
	}

}