package org.processmining.stochasticlabelleddatapetrinet.probability;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapterImpl;
import org.processmining.stochasticlabelledpetrinets.probability.CrossProductResultSolver;

import lpsolve.LpSolveException;

public class DataUEMSC {

	public static double getTraceProbability(StochasticLabelledDataPetriNetSemantics semantics, XTrace trace,
			XEventClassifier clasifier, ProMCanceller canceller) throws LpSolveException {
		DataStateLogAdapter logAdapter = new DataStateLogAdapterImpl(semantics);

		CrossProductResultSolver result = new CrossProductResultSolver();
		FollowerSemanticsDataImpl systemB = new FollowerSemanticsDataImpl(trace, clasifier, semantics, logAdapter);
		CrossProductSLDPN.traverse(semantics, systemB, result, canceller);

		return result.solve(canceller);
	}

}