package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;

import com.opencsv.CSVWriter;

@Plugin(name = "Stochastic labelled data Petri net - One Hot Encoding exporter", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Stochastic labelled data Petri net", "File" }, userAccessible = true)
@UIExportPlugin(description = "SLDPN One Hot Encoding", extension = "csv")
public class SLDPNCSVOneHotExportPlugin {


	@PluginVariant(variantLabel = "SLDPN One Hot Encoding", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, SLDPN net, File file) throws IOException {

		try (FileWriter fw = new FileWriter(file)) {
			try (CSVWriter writer = new CSVWriter(fw)) {
	
				OneHotEncoding ohe = net.getOneHotEncoding();
				
				String[] header = new String[3];
				header[0] = "variable";
				header[1] = "variable_value";
				header[2] = "variable_onehot";
				writer.writeNext(header);
				
				Set<String> variables = ohe.getVariables();
				
				for (String var: variables) {

					Map<Object, String> mapping = ohe.getMapping(var);
					
					for (Entry<Object, String> entry: mapping.entrySet()) {

						String[] oheMapping = new String[3];
						
						oheMapping[0] = var;
						oheMapping[1] = toString(entry);
						oheMapping[2] = entry.getValue();
						
						writer.writeNext(oheMapping);
					}
				}
				
				writer.flush();
			}
		}
		
	}

	private String toString(Entry<Object, String> entry) {
		Object val = entry.getKey();
		return val != null ? val.toString() : "NULL";
	}

}  