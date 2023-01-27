package org.processmining.stochasticlabelleddatapetrinet.weights;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class LogisticWeightFunction implements SerializableWeightFunction {

	private double[] coefficients; // beta_1..m
	private double intercept; // beta_0
	
	public LogisticWeightFunction() {
		super(); // for deserialization, needs to be public to be accessible by serializer
	}

	public LogisticWeightFunction(double intercept, double[] coefficients) {
		super();
		this.coefficients = coefficients;
		this.intercept = intercept;
	}

	public double evaluateWeight(StochasticLabelledDataPetriNet net, DataState dataState) {
		if (coefficients.length != dataState.capacity()) {
			throw new IllegalArgumentException("Mismatch between DataState and learned coefficients. Expecting "
					+ coefficients.length + " variables but received " + dataState.capacity());
		}

		double weight = intercept;
		for (int i = 0; i < dataState.capacity(); i++) {
			if (dataState.contains(i)) {
				if (net.getVariableType(i) == VariableType.DISCRETE) {
					weight += dataState.getLong(i) * coefficients[i];
				} else {
					weight += dataState.getDouble(i) * coefficients[i];	
				}				
			} else {
				// We are ignoring the missing variable 
			}
		}
		return sigmoid(weight);
	}

	private static double sigmoid(double x) {
		return 1.0d / (1.0d + Math.exp(-x)); 
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

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(coefficients);
		result = prime * result + Objects.hash(intercept);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LogisticWeightFunction other = (LogisticWeightFunction) obj;
		return Arrays.equals(coefficients, other.coefficients)
				&& Double.doubleToLongBits(intercept) == Double.doubleToLongBits(other.intercept);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format(Locale.ENGLISH, "%.3f+[", intercept));		
		for (int i = 0; i < coefficients.length; i++) {
			double d = coefficients[i];
			sb.append(String.format(Locale.ENGLISH, "%.3f", d));
			if (i < coefficients.length-1) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
}
