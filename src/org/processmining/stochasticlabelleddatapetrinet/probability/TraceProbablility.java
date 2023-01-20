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

		String[] activitySequence = getActivitySequence(trace, classifier);
		DataState[] dataSequence = getDataSequence(trace, logAdapter);

		return getTraceProbability(semantics, activitySequence, dataSequence, canceller);
	}

	public static double getTraceProbability(StochasticLabelledDataPetriNetSemantics semantics,
			String[] activitySequence, DataState[] dataSequence, ProMCanceller canceller) throws LpSolveException {

		CrossProductResultSolver result = new CrossProductResultSolver();
		FollowerSemanticsDataImpl systemB = new FollowerSemanticsDataImpl(activitySequence, dataSequence);
		CrossProductSLDPN.traverse(semantics, systemB, result, canceller);

		//		CrossProductResultDot result2 = new CrossProductResultDot();
		//		FollowerSemanticsDataImpl systemB2 = new FollowerSemanticsDataImpl(activitySequence, dataSequence);
		//		CrossProductSLDPN.traverse(semantics, systemB2, result2, canceller);
		//		System.out.println(result2.toDot());

		return result.solve(canceller);
	}

	public static String[] getActivitySequence(XTrace trace, XEventClassifier classifier) {
		String[] activityTrace = new String[trace.size()];
		for (int i = 0; i < trace.size(); i++) {
			XEvent event = trace.get(i);

			activityTrace[i] = classifier.getClassIdentity(event);

		}
		return activityTrace;
	}

	public static DataState[] getDataSequence(XTrace trace, DataStateLogAdapter logAdapter) {
		DataState[] dataTrace = new DataState[trace.size()];

		DataState data = logAdapter.fromTrace(trace);

		for (int i = 0; i < trace.size(); i++) {
			XEvent event = trace.get(i);

			dataTrace[i] = data;

			data = data.deepCopy();
			data = logAdapter.fromEvent(event, data);
		}
		return dataTrace;
	}
}