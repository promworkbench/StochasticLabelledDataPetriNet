package org.processmining.stochasticlabelleddatapetrinet.weights.writeops;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependent;

public interface WriteOperationMiner {

	public StochasticLabelledDataPetriNetWeightsDataDependent extendWithWrites(StochasticLabelledDataPetriNet<?> net);

}
