package org.processmining.stochasticlabelleddatapetrinet.weights;

public class SimpleLinearWeightFunction extends LinearWeightFunction {

	/**
	 * Weight function for a linear weight function based on a single variable
	 * 
	 * @param intercept
	 * @param coefficient
	 * @param variableIdx
	 */
	public SimpleLinearWeightFunction(double intercept, double coefficient, int variableIdx, int numVariables) {
		super(intercept, buildCoefficients(coefficient, variableIdx, numVariables));
	}

	private static double[] buildCoefficients(double coefficient, int variableIdx, int numVariables) {
		double[] coefficients = new double[numVariables];
		coefficients[variableIdx] = coefficient;
		return coefficients;
	}

}
