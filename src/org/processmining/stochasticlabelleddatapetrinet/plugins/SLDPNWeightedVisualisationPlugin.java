package org.processmining.stochasticlabelleddatapetrinet.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class SLDPNWeightedVisualisationPlugin  {

	@Plugin(name = "Stochastic labelled Data Petri net (SLDPN) - replay", returnLabels = {
	"SLDPN visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"SLDPN", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Felix Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(variantLabel = "Stochastic labelled Data Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, StochasticLabelledDataPetriNetWeights net,
		ProMCanceller canceller) {
		SLDPNVisualizer<StochasticLabelledDataPetriNetWeights> visualizer = new SLDPNVisualizer<StochasticLabelledDataPetriNetWeights>(net) {

			protected double getTransitionWeight(int a, DataState dataState) {
				return net.getTransitionWeight(a, dataState);
			}};
		return visualizer.visualiseNet();
	}	
	
	@Plugin(name = "Stochastic labelled Data Petri net (SLDPN) - replay", returnLabels = {
	"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"SLDPN", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Felix Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(variantLabel = "Stochastic labelled Data Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualise(final PluginContext context, SLDPN net,
		ProMCanceller canceller) {
		return visualise(context, net.getModel(), canceller);
	}
	
	@Plugin(name = "Stochastic labelled Data Petri net (SLDPN) - weight function", returnLabels = {
	"Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = {
			"SLDPN", "canceller" }, userAccessible = true, level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Felix Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(variantLabel = "Stochastic labelled Data Petri net visualisation", requiredParameterLabels = { 0, 1 })
	public JComponent visualiseWeightFunction(final PluginContext context, SLDPN net,
		ProMCanceller canceller) {
		SLDPNVisualizerWeightFunctionParameters<StochasticLabelledDataPetriNetWeights> visualizer = new SLDPNVisualizerWeightFunctionParameters<StochasticLabelledDataPetriNetWeights>(net.getModel()) {

			protected String getWeightFunctionParameters(int a) {
				return net.getModel().getWeightFunction(a).toString();
			} };

		return visualizer.visualiseNet();
	}
	
	@Plugin(
			name = "(Prettier) Stochastic labelled Data "
					+ "Petri net (SLDPN) - weight function",
			returnLabels = {"Dot visualization" }, 
			returnTypes = { JComponent.class }, 
			parameterLabels = {	"SLDPN", "canceller" }, 
			userAccessible = true, 
			level = PluginLevel.Regular)
	@Visualizer
	@UITopiaVariant(
			affiliation = "QUT", 
			author = "Adam Banham", 
			email = "adam_banham@hotmail.com")
	@PluginVariant(
			variantLabel = "(Prettier) "
					+ "Stochastic labelled Data Petri net visualisation",
			requiredParameterLabels = { 0, 1 }
	)
	public JComponent visualisePrettierWeightFunction(
			final PluginContext context, SLDPN net,
			ProMCanceller canceller) {
		return SLDPNStaticPettierVisualizer.visualise(net);
	}
	
	

	
}
