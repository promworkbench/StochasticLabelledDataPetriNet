package org.processmining.stochasticlabelleddatapetrinet;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

/**
 * Weight is constant regardless of data values.
 * 
 * @author F. Mannhardt
 *
 */
public class ConstantWeightFunction implements WeightFunction {
	
	private final double weight;
	
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

}
