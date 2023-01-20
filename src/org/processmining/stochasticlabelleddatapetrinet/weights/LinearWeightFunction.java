package org.processmining.stochasticlabelleddatapetrinet.weights;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class LinearWeightFunction implements WeightFunction {
	
	private final double[] coefficients;
	private final double intercept;
	
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

}
