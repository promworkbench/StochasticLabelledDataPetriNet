package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;
import org.processmining.stochasticlabelleddatapetrinet.weights.LogisticWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.WeightFunction;

import com.opencsv.CSVWriter;

@Plugin(name = "Stochastic labelled data Petri net exporter", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Stochastic labelled data Petri net", "File" }, userAccessible = true)
@UIExportPlugin(description = "SLDPN Weights", extension = "csv")
public class SLDPNCSVExportPlugin {


	@PluginVariant(variantLabel = "SLDPN export", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, SLDPN net, File file) throws IOException {

		try (FileWriter fw = new FileWriter(file)) {
			try (CSVWriter writer = new CSVWriter(fw)) {
	
				StochasticLabelledDataPetriNetWeights model = net.getModel();
				OneHotEncoding ohe = net.getOneHotEncoding();
				
				String[] header = new String[2 + model.getNumberOfVariables()];
				header[0] = "transition";
				header[1] = "intercept";
				for (int i = 0; i < model.getNumberOfVariables(); i++) {
					header[1+i] = model.getVariableLabel(i);
				}
				writer.writeNext(header);
				
				for (int j = 0; j < model.getNumberOfTransitions(); j++) {
					
					String[] weights = new String[2 + model.getNumberOfVariables()];
					
					if (model.isTransitionSilent(j)) {
						weights[0] = "inv"+j;
					} else {
						weights[0] = model.getTransitionLabel(j);	
					}
					
					WeightFunction weightFunction = model.getWeightFunction(j);
					if (weightFunction instanceof LogisticWeightFunction) {
						LogisticWeightFunction lwf = (LogisticWeightFunction) weightFunction;
						weights[1] = String.valueOf(lwf.getIntercept());
						
						double[] coeff = lwf.getCoefficients();
						for (int i = 0; i < coeff.length; i++) {
							weights[2+i] = String.valueOf(coeff[i]);
						}
						
					} else {
						for (int i = 1; i < weights.length; i++) {
							weights[i] = "NA";
						}
					}
					
					writer.writeNext(weights);
				}
				
				writer.flush();
			}
		}
		
	}

} 