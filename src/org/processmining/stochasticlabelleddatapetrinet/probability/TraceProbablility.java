package org.processmining.stochasticlabelleddatapetrinet.probability;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelledpetrinets.probability.CrossProductResultSolver;

import lpsolve.LpSolveException;

public class TraceProbablility {

	public static double getTraceProbability(StochasticLabelledDataPetriNetSemantics semantics,
			String[] activitySequence, DataState[] dataSequence, ProMCanceller canceller) throws LpSolveException {

		CrossProductResultSolver result = new CrossProductResultSolver();
		FollowerSemanticsDataImpl systemB = new FollowerSemanticsDataImpl(activitySequence, dataSequence);
		CrossProductSLDPN.traverse(semantics, systemB, result, canceller);

		//		{
		//			CrossProductResultDot result2 = new CrossProductResultDot();
		//			FollowerSemanticsDataImpl systemB2 = new FollowerSemanticsDataImpl(activitySequence, dataSequence);
		//			CrossProductSLDPN.traverse(semantics, systemB2, result2, canceller);
		//			System.out.println(result2.toDot());
		//			System.out.println(Arrays.toString(activitySequence));
		//			System.out.println(Arrays.toString(dataSequence));
		//		}

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

	public static DataState[] getDataSequence(XTrace trace, DataStateLogAdapter logAdapter, int minimumTraceLength) {
		DataState[] dataTrace = new DataState[Math.max(trace.size() + 1, minimumTraceLength + 1)];

		DataState data = logAdapter.fromTrace(trace);
		dataTrace[0] = data;

		for (int i = 0; i < trace.size(); i++) {
			XEvent event = trace.get(i);

			dataTrace[i + 1] = data;

			data = data.deepCopy();
			data = logAdapter.fromEvent(event, data);
		}
		for (int i = trace.size() + 1; i < minimumTraceLength + 1; i++) {
			dataTrace[i] = dataTrace[i - 1];
		}
		return dataTrace;
	}
}