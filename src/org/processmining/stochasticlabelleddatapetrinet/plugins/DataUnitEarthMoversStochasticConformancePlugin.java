package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.duemsc.duEMSC;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapterImpl;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;

import lpsolve.LpSolveException;

public class DataUnitEarthMoversStochasticConformancePlugin {

	public static double measureLogModel(XLog log, XEventClassifier classifier, SLDPN model, boolean debug,
			ProMCanceller canceller) throws LpSolveException {

		OneHotEncoding encoder = model.getOneHotEncoding();
		XLog log2 = encoder.process(log);

		StochasticLabelledDataPetriNetSemantics semanticsB = model.getModel().getDefaultSemantics();

		return duEMSC.compute(log2, classifier, new DataStateLogAdapterImpl(semanticsB), semanticsB, canceller);
	}

}