package org.processmining.stochasticlabelleddatapetrinet.probability;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapterImpl;
import org.processmining.stochasticlabelledpetrinets.probability.CrossProductResultSolver;

import lpsolve.LpSolveException;

public class TraceProbablility {

	public static double getConditionalTraceProbability(StochasticLabelledDataPetriNetSemantics semantics, XTrace trace,
			XEventClassifier classifier, ProMCanceller canceller) throws LpSolveException {
		DataStateLogAdapter logAdapter = new DataStateLogAdapterImpl(semantics);

		String[] activityTrace = new String[trace.size()];
		DataState[] dataTrace = new DataState[trace.size()];

		DataState data = logAdapter.fromTrace(trace);

		for (int i = 0; i < trace.size(); i++) {
			XEvent event = trace.get(i);

			activityTrace[i] = classifier.getClassIdentity(event);
			dataTrace[i] = data;

			data = data.deepCopy();
			data = logAdapter.fromEvent(event, data);
		}

		return getTraceProbability(semantics, activityTrace, dataTrace, canceller);
	}

	public static double getTraceProbability(StochasticLabelledDataPetriNetSemantics semantics, String[] activityTrace,
			DataState[] dataTrace, ProMCanceller canceller) throws LpSolveException {

		CrossProductResultSolver result = new CrossProductResultSolver();
		FollowerSemanticsDataImpl systemB = new FollowerSemanticsDataImpl(activityTrace, dataTrace);
		CrossProductSLDPN.traverse(semantics, systemB, result, canceller);

		return result.solve(canceller);
	}

}