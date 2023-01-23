package org.processmining.stochasticlabelleddatapetrinet.weights.writeops;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;

public interface WriteOperationMiner {
	
	public StochasticLabelledDataPetriNet extendWithWrites(StochasticLabelledDataPetriNet net);

}
