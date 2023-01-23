package org.processmining.stochasticlabelleddatapetrinet.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;

public interface StochasticLabelledDataPetriNetSerializer<T extends StochasticLabelledDataPetriNet<T>> {

	void serialize(T net, OutputStream os) throws IOException;

	T deserialize(InputStream is) throws IOException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException;

}