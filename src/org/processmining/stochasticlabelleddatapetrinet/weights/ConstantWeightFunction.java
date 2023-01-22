package org.processmining.stochasticlabelleddatapetrinet.weights;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

/**
 * Weight is constant regardless of data values.
 * 
 * @author F. Mannhardt
 *
 */
public class ConstantWeightFunction implements SerializableWeightFunction {
	
	private double weight;
	
	public ConstantWeightFunction() {
		super();
		this.weight = 1;
	}

	public ConstantWeightFunction(double weight) {
		this.weight = weight;
	}

	public double evaluateWeight(DataState dataState) {
		return weight;
	}

	public void serialize(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(0); // version reserved
		dos.writeDouble(weight);
		dos.flush();
	}

	public void deserialize(InputStream is) throws IOException {		
		DataInputStream dis = new DataInputStream(is);
		dis.readInt();
		weight = dis.readDouble();
	}
}
