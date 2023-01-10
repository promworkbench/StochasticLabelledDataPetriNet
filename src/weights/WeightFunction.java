package weights;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public interface WeightFunction {

	double evaluateWeight(DataState dataState);		

}