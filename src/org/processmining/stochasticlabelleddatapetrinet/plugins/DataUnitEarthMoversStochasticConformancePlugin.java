package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.duemsc.duEMSC;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapterImpl;

import lpsolve.LpSolveException;

public class DataUnitEarthMoversStochasticConformancePlugin {

	public static double measureLogModel(XLog log, XEventClassifier classifier,
			StochasticLabelledDataPetriNetSemantics semanticsB, boolean debug, ProMCanceller canceller)
			throws LpSolveException {
		return duEMSC.compute(log, classifier, new DataStateLogAdapterImpl(semanticsB), semanticsB, canceller);
	}

}