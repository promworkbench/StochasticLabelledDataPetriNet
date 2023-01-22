package org.processmining.stochasticlabelleddatapetrinet.weights;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class LinearWeightFunction implements SerializableWeightFunction  {
	
	private double[] coefficients;
	private double intercept;
	
	LinearWeightFunction() {
		super(); // for deserialization
	}
	
	public LinearWeightFunction(double intercept, double[] coefficients) {
		super();
		this.coefficients = coefficients;
		this.intercept = intercept;
	}

	public double evaluateWeight(DataState dataState) {
		double weight = intercept;
		for (int i = 0; i < dataState.capacity(); i++) {
			if (dataState.contains(i)) {
				weight += dataState.getDouble(i) * coefficients[i];
			} else {
				// how to treat missing value
			}
		}
		return weight;
	}
	
	public void serialize(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(0); // version reserved
		dos.writeDouble(intercept);
		dos.writeInt(coefficients.length);
		for (int i = 0; i < coefficients.length; i++) {
			dos.writeDouble(coefficients[i]);
		}
		dos.flush();
	}

	public void deserialize(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream(is);
		dis.readInt();
		intercept = dis.readDouble();
		coefficients = new double[dis.readInt()];
		for (int i = 0; i < coefficients.length; i++) {
			coefficients[i] = dis.readDouble();
		}
	}	

}
