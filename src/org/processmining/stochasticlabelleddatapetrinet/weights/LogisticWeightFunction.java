package org.processmining.stochasticlabelleddatapetrinet.weights;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class LogisticWeightFunction implements WeightFunction {
	
	private final double[] coefficients; // beta_1..m
	private final double intercept; // beta_0
	
	public LogisticWeightFunction(double intercept, double[] coefficients) {
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
				//TODO how to treat missing value
			}
		}
		return sigmoid(weight);
	}
	
	private static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.exp(-x));
	}	

}
