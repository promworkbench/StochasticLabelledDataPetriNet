package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Stochastic labelled data Petri net exporter", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Stochastic labelled data Petri net", "File" }, userAccessible = true)
@UIExportPlugin(description = "Stochastic labelled data Petri net", extension = "sldpn")
public class SLDPNExportPlugin {

	public static final String FILENAME_MODEL = "model.dat";
	public static final String FILENAME_ENCODING = "encoding.dat";

	@PluginVariant(variantLabel = "SLDPN export", requiredParameterLabels = { 0, 1 })
	public void exportDefault(UIPluginContext context, SLDPN net, File file) throws IOException {
		export(net, file);
	}

	public static void export(SLDPN sldpn, File file) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));

		//write model
		{
			ZipEntry e = new ZipEntry(FILENAME_MODEL);
			out.putNextEntry(e);
			sldpn.getModel().getDefaultSerializer().serialize(sldpn.getModel(), out);
			out.closeEntry();
		}

		//write one-hot-encoding
		{
			ZipEntry e = new ZipEntry(FILENAME_ENCODING);
			out.putNextEntry(e);
			sldpn.getOneHotEncoding().serialize(out);
			out.closeEntry();
		}

		out.close();
	}
}