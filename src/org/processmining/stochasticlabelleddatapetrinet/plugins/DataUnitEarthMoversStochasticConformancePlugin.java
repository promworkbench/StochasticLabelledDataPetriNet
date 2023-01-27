package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.duemsc.duEMSC;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapterImpl;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;

import lpsolve.LpSolveException;

public class DataUnitEarthMoversStochasticConformancePlugin {

	@Plugin(name = "Check conformance of a stochastic labelled data Petri net and an event log using Data-Aware Unit Earth Movers' Stochastic Conformance duemsc", level = PluginLevel.Regular, returnLabels = {
			"Stochastic labelled data Petri net" }, returnTypes = { HTMLToString.class }, parameterLabels = {
					"stochastic labelled data Petri net (SLDPN)", "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Check an SLDPN, dialog", requiredParameterLabels = { 0, 1 })
	public HTMLToString mine(PluginContext context, SLDPN model, XLog xLog) throws Exception {
		ProMCanceller canceller = new ProMCanceller() {
			public boolean isCancelled() {
				return context.getProgress().isCancelled();
			}
		};
		double result = measureLogModel(xLog, new XEventNameClassifier(), model, false, canceller);
		return new HTMLToString() {

			public String toHTMLString(boolean includeHTMLTags) {
				return "data-aware Earth Movers' Stochastic Conformance: " + result;
			}
		};
	}

	public static double measureLogModel(XLog log, XEventClassifier classifier, SLDPN model, boolean debug,
			ProMCanceller canceller) throws LpSolveException {

		OneHotEncoding encoder = model.getOneHotEncoding();
		XLog log2;
		if (encoder != null) {
			log2 = encoder.process(log);
		} else {
			log2 = log;
		}

		StochasticLabelledDataPetriNetSemantics semanticsB = model.getModel().getDefaultSemantics();

		return duEMSC.compute(log2, classifier, new DataStateLogAdapterImpl(semanticsB), semanticsB, canceller);
	}

}