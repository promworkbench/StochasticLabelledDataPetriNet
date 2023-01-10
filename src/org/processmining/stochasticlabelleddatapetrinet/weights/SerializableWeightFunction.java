package org.processmining.stochasticlabelleddatapetrinet.weights;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SerializableWeightFunction extends WeightFunction {
	
	void serialize(OutputStream os) throws IOException;
	
	void deserialize(InputStream is) throws IOException;

}
