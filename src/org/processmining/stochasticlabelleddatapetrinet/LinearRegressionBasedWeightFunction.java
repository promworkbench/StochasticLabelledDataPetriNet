package org.processmining.stochasticlabelleddatapetrinet;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

import weights.WeightFunction;

public class LinearRegressionBasedWeightFunction implements WeightFunction {
	
	private final double[] coefficients;
	private final double intercept;
	
	public LinearRegressionBasedWeightFunction(double intercept, double[] coefficients) {
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

}
