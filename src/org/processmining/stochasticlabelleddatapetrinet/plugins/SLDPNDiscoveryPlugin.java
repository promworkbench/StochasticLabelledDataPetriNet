package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.LogisticRegressionWeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.WeightFitter;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.WeightFitterException;
import org.processmining.stochasticlabelleddatapetrinet.weights.writeops.AllWriteOperationMiner;

public class SLDPNDiscoveryPlugin {

	@Plugin(name = "Discover stochastic data model on an accepting Petri net", level = PluginLevel.Regular, returnLabels = {
			"Stochastic labelled data Petri net" }, returnTypes = {
					SLDPN.class }, parameterLabels = { "Accepting Petri net", "Log" }, userAccessible = true)
	@UITopiaVariant(affiliation = IMMiningDialog.affiliation, author = IMMiningDialog.author, email = IMMiningDialog.email)
	@PluginVariant(variantLabel = "Mine an SLDPN, dialog", requiredParameterLabels = { 0, 1 })
	public SLDPN mine(UIPluginContext context, AcceptingPetriNet model, XLog xLog) throws Exception {
		return discover(xLog, model, new SLDPNDiscoveryParametersDefault());
	}

	/**
	 * Discover a stochastic labelled data Petri net by annotating the given
	 * control flow model. This method will copy the log.
	 * 
	 * @param log
	 * @param controlFlowModel
	 * @param classifier
	 * @return
	 * @throws WeightFitterException
	 */
	public static SLDPN discover(XLog log, AcceptingPetriNet controlFlowModel, SLDPNDiscoveryParameters parameters)
			throws WeightFitterException {
		StochasticLabelledDataPetriNet dnet = PetrinetConverter.viewAsSLDPN(controlFlowModel);

		OneHotEncoding encoder;
		XLog encodedLog;
		if (parameters.isOneHotEncoding()) {
			encoder = new OneHotEncoding(parameters.getClassifier().getDefiningAttributeKeys());

			// learn the encoding
			encoder.fit(log);

			// apply
			encodedLog = encoder.process(log);
		} else {
			encoder = null;
			encodedLog = log;
		}

		AllWriteOperationMiner writeOpMiner = new AllWriteOperationMiner(encodedLog);

		dnet = writeOpMiner.extendWithWrites(dnet);

		WeightFitter fitter = new LogisticRegressionWeightFitter(parameters.getClassifier());

		StochasticLabelledDataPetriNetWeights netWithWeights = fitter.fit(encodedLog, dnet);

		return new SLDPN(encoder, netWithWeights);
	}
}