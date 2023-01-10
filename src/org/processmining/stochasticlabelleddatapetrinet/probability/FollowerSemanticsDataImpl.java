package org.processmining.stochasticlabelleddatapetrinet.probability;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;

public class FollowerSemanticsDataImpl implements FollowerSemanticsData<Integer, DataState> {

	private final XTrace trace;
	private final XEventClassifier classifier;
	private final DataStateLogAdapter dataLogAdapter;

	public FollowerSemanticsDataImpl(XTrace trace, XEventClassifier classifier,
			StochasticLabelledDataPetriNetSemantics semantics, DataStateLogAdapter logAdapter) {
		this.trace = trace;
		this.classifier = classifier;
		this.dataLogAdapter = logAdapter;
	}

	public Pair<Integer, DataState> getInitialState() {
		DataState dataState = dataLogAdapter.fromTrace(trace);
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
		return dataLogAdapter.fromEvent(trace.get(stateB));
	}

}