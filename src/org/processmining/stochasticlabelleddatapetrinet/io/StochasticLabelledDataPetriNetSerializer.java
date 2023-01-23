package org.processmining.stochasticlabelleddatapetrinet.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;

public interface StochasticLabelledDataPetriNetSerializer {

	void serialize(StochasticLabelledDataPetriNet net, OutputStream os) throws IOException;

	StochasticLabelledDataPetriNet deserialize(InputStream is)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException;

}