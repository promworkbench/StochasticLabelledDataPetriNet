package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;

public class SLDPN {

	private final OneHotEncoding oneHotEncoding;
	private final StochasticLabelledDataPetriNet model;

	public SLDPN(OneHotEncoding oneHotEncoding, StochasticLabelledDataPetriNet model) {
		this.oneHotEncoding = oneHotEncoding;
		this.model = model;
	}

	public OneHotEncoding getOneHotEncoding() {
		return oneHotEncoding;
	}

	public StochasticLabelledDataPetriNet getModel() {
		return model;
	}

}