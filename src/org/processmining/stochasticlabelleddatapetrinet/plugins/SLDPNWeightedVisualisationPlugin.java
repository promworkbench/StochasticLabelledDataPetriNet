package org.processmining.stochasticlabelleddatapetrinet.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class SLDPNWeightedVisualisationPlugin  {

	@Plugin(name = "Stochastic labelled Data Petri net (SLDPN) visualisation", returnLabels = {
	"SLDPN visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"SLDPN", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Felix Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(variantLabel = "Stochastic labelled Data Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, StochasticLabelledDataPetriNetWeights net,
		ProMCanceller canceller) {
		return createVisualisation(net);
	}	
	
	@Plugin(name = "Stochastic labelled Data Petri net (SLDPN) visualisation", returnLabels = {
	"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"SLDPN", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Felix Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(variantLabel = "Stochastic labelled Data Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, SLDPN net,
		ProMCanceller canceller) {
		return createVisualisation(net.getModel());
	}

	DotPanel createVisualisation(final StochasticLabelledDataPetriNetWeights net) {
		SLDPNVisualizer<StochasticLabelledDataPetriNetWeights> visualizer = new SLDPNVisualizer<StochasticLabelledDataPetriNetWeights>(net) {

			protected double getTransitionWeight(int a, DataState dataState) {
				return net.getTransitionWeight(a, dataState);
			}};
		return visualizer.visualiseNet();
	}		
	
}
