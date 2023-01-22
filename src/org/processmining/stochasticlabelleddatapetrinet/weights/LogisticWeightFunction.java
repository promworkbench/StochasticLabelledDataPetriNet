package org.processmining.stochasticlabelleddatapetrinet.weights;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class LogisticWeightFunction implements SerializableWeightFunction {

	private double[] coefficients; // beta_1..m
	private double intercept; // beta_0
	
	LogisticWeightFunction() {
		super(); // for deserialization
	}

	public LogisticWeightFunction(double intercept, double[] coefficients) {
		super();
		this.coefficients = coefficients;
		this.intercept = intercept;
	}

	public double evaluateWeight(DataState dataState) {
		if (coefficients.length != dataState.capacity()) {
			throw new IllegalArgumentException("Mismatch between DataState and learned coefficients. Expecting "
					+ coefficients.length + " variables but received " + dataState.capacity());
		}

		double weight = intercept;
		for (int i = 0; i < dataState.capacity(); i++) {
			if (dataState.contains(i)) {
				weight += dataState.getDouble(i) * coefficients[i];
			} else {
				//TODO how to treat missing value, for now we simply do not consider the value at all
			}
		}
		return sigmoid(weight);
	}

	private static double sigmoid(double x) {
		return 1.0d / (1.0d + Math.exp(-x)); //TODO look at numeric stability, there is some warning in WEKA code 
	}

	public void serialize(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(0); // version reserved
		dos.writeDouble(intercept);
		dos.writeInt(coefficients.length);
		for (int i = 0; i < coefficients.length; i++) {
			dos.writeDouble(coefficients[i]);
		}
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
