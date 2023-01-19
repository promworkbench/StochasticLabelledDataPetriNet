package org.processmining.stochasticlabelleddatapetrinet.pnadapater;

import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;

public class PetrinetConverter {
	
	public interface PetrinetMarkedWithMappings {
		
		Petrinet getNet();

		Marking getInitialMarking();

		Marking[] getFinalMarkings();
		
		Map<Integer, String> getTransitionIndexToId();

	}

	public static PetrinetMarkedWithMappings viewAsPetrinet(StochasticLabelledDataPetriNet sldpn) {
		
		PetrinetImpl pn = new PetrinetImpl("Converted from " + sldpn.toString());
		Map<Integer, String> conversionMap = new HashMap<>();
		
		Transition[] transitions = new Transition[sldpn.getNumberOfPlaces()];
		Place[] places = new Place[sldpn.getNumberOfPlaces()];
		
		for (int i = 0; i < sldpn.getNumberOfTransitions(); i++) {
			transitions[i] = pn.addTransition(sldpn.getTransitionLabel(i));
			conversionMap.put(i, transitions[i].getLocalID().toString());
		}
		
		for (int i = 0; i < sldpn.getNumberOfPlaces(); i++) {
			places[i] = pn.addPlace("p" + i);
		}
			
		for (int i = 0; i < sldpn.getNumberOfTransitions(); i++) {
			for (int placeIdx: sldpn.getInputPlaces(i)) {
				pn.addArc(places[placeIdx], transitions[i]);
			}
			for (int placeIdx: sldpn.getOutputPlaces(i)) {
				pn.addArc(transitions[i], places[placeIdx]);
			}
		}
		
		Marking initialMarking = new Marking();
		for (int i = 0; i < sldpn.getNumberOfPlaces(); i++) {
			if (sldpn.isInInitialMarking(i) > 0) {
				initialMarking.add(places[i], sldpn.isInInitialMarking(i));				
			}
		}
		Marking[] finalMarkings = new Marking[0]; // We don't have that information
		
		return new PetrinetMarkedWithMappings() {
			
			public Petrinet getNet() {
				return pn;
			}
			
			public Marking getInitialMarking() {
				return initialMarking;
			}
			
			public Marking[] getFinalMarkings() {
				return finalMarkings;
			}

			public Map<Integer, String> getTransitionIndexToId() {
				return conversionMap;
			}
		};
	}

}
