package org.processmining.stochasticlabelleddatapetrinet.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;

public class SLDPNWeightedVisualisationPlugin extends SLDPNVisualisationPlugin<StochasticLabelledDataPetriNetWeights> {

	@Plugin(name = "Stochastic labelled Data Petri net (SLDPN) visualisation", returnLabels = {
	"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"SLDPN", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Felix Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(variantLabel = "Stochastic labelled Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, StochasticLabelledDataPetriNetWeights net,
		ProMCanceller canceller) {
		return visualise(net);
	}	
	
	@Plugin(name = "Stochastic labelled Data Petri net (SLDPN) visualisation", returnLabels = {
	"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"SLDPN", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Felix Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(variantLabel = "Stochastic labelled Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, SLDPN net,
		ProMCanceller canceller) {
	return visualise(net.getModel());
	}		
	
	public void decoratePlace(StochasticLabelledDataPetriNetWeights net, int place, DotNode dotNode) {
	}

	public void decorateTransition(StochasticLabelledDataPetriNetWeights net, int transition, DotNode dotNode) {
		dotNode.setOption("xlabel", net.getWeightFunction(transition).toString() + "");
	}

}
