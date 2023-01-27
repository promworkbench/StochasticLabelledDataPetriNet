package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class SLDPNWeightedVisualisationPluginTest {
	
	@Test
	public void testVisualisation() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, IOException {
		
		try (InputStream model = getClass().getResourceAsStream("SampleLog10000.xes.gz-0.sldpn")) {
			SLDPN sldpn = SLDPNImportPlugin.read(model);
			//new SLDPNWeightedVisualisationPlugin().createVisualisation(sldpn.getModel());		
		}		
				
	}

}
