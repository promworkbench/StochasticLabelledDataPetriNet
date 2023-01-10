package org.processmining.stochasticlabelleddatapetrinet.probability;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.stochasticlabelledpetrinets.probability.CrossProductResultSolver;

import lpsolve.LpSolveException;

public class DataUEMSC {

	public static double getTraceProbability(StochasticLabelledDataPetriNetSemantics semantics, XTrace trace,
			XEventClassifier clasifier, ProMCanceller canceller) throws LpSolveException {
		semantics.setInitialState();

		CrossProductResultSolver result = new CrossProductResultSolver();
		FollowerSemanticsDataImpl systemB = new FollowerSemanticsDataImpl(trace, clasifier);
		CrossProductSLDPN.traverse(semantics, systemB, result, canceller);

		return result.solve(canceller);
	}

}