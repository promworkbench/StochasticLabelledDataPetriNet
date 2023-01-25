package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;

public class SLDPN {

	private final OneHotEncoding oneHotEncoding;
	private final StochasticLabelledDataPetriNetWeights model;

	public SLDPN(OneHotEncoding oneHotEncoding, StochasticLabelledDataPetriNetWeights model) {
		this.oneHotEncoding = oneHotEncoding;
		this.model = model;
	}

	public OneHotEncoding getOneHotEncoding() {
		return oneHotEncoding;
	}

	public StochasticLabelledDataPetriNetWeights getModel() {
		return model;
	}

}