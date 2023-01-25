package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.io.OneHotEncodingSerializerImpl;
import org.processmining.stochasticlabelleddatapetrinet.io.StochasticLabelledDataPetriNetSerializerImpl;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;

@Plugin(name = "Stochastic labelled data Petri net importer", parameterLabels = { "File" }, returnLabels = {
		"Stochastic labelled data Petri net" }, returnTypes = { SLDPN.class })
@UIImportPlugin(description = "Stochastic labelled data Petri net file", extensions = { "sldpn" })
public class SLDPNImportPlugin extends AbstractImportPlugin {

	public SLDPN importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		return read(input);
	}

	public static SLDPN read(InputStream in) throws IOException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));

		ZipEntry ze;

		StochasticLabelledDataPetriNetWeights model = null;
		OneHotEncoding encoding = null;

		while ((ze = zis.getNextEntry()) != null) {
			if (ze.getName().equals(SLDPNExportPlugin.FILENAME_MODEL)) {
				model = new StochasticLabelledDataPetriNetSerializerImpl().deserialize(zis);
			} else if (ze.getName().equals(SLDPNExportPlugin.FILENAME_ENCODING)) {
				encoding = new OneHotEncodingSerializerImpl().deserialize(zis);
			}
			zis.closeEntry();
		}
		zis.close();

		if (model == null || encoding == null) {
			throw new RuntimeException("File is not a valid SLDPN.");
		}
		return new SLDPN(encoding, model);
	}
}