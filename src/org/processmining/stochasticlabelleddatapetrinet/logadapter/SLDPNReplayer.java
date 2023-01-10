package org.processmining.stochasticlabelleddatapetrinet.logadapter;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XUtils;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class SLDPNReplayer {
	
	
	public SLDPNReplayer() {
		
	}
	
	
	public double replay(StochasticLabelledDataPetriNet sldpn, XTrace trace) {
		
		StochasticLabelledDataPetriNetSemantics semantics = sldpn.getDefaultSemantics();
		DataStateLogAdapter adapter = new DataStateLogAdapterImpl(semantics);
		
		Map<String, Integer> transitionIndicies = new HashMap<>();
		for (int i = 0; i < sldpn.getNumberOfTransitions(); i++) {
			transitionIndicies.put(sldpn.getTransitionLabel(i), i);
		}
		
		double sumOfWeights = 0.0; 
		
		semantics.setInitialState(adapter.fromTrace(trace));		
		
		for (XEvent event: trace) {
			
			//TODO classifiers
			String name = XUtils.getConceptName(event);			
			Integer idx = transitionIndicies.get(name);
			
			BitSet enabledTransitions = semantics.getEnabledTransitions();
			
			if (idx != null && enabledTransitions.get(idx)) {
								
												
				DataState writeState = adapter.fromEvent(event);
								
				System.out.println("Executing transition " + name + "(" + idx + ") with weight " + semantics.getTransitionWeight(idx) + 
								   " and probability " + (semantics.getTransitionWeight(idx) / semantics.getTotalWeightOfEnabledTransitions()));
				
				sumOfWeights += semantics.getTransitionWeight(idx);
				
				semantics.executeTransition(idx, writeState);
			
			} else {
				throw new IllegalStateException("Cannot execute transition, not enabled!");
			}	
						
		}
		
		if (semantics.isFinalState()) {
			return sumOfWeights;
		} else {
			return -1;
		}		
		
	}
	
	

}
