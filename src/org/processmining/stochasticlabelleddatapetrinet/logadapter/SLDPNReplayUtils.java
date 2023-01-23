package org.processmining.stochasticlabelleddatapetrinet.logadapter;

import java.util.HashMap;
import java.util.Map;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;

public final class SLDPNReplayUtils {

	private SLDPNReplayUtils() {
	}
	
	public static Map<String, Integer> buildTransitionMap(StochasticLabelledDataPetriNet sldpn) {
		Map<String, Integer> transitionIndicies = new HashMap<>();
		for (int i = 0; i < sldpn.getNumberOfTransitions(); i++) {
			transitionIndicies.put(sldpn.getTransitionLabel(i), i);
		}		
		return transitionIndicies;
	}
	
}
