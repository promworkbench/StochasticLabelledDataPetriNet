package org.processmining.stochasticlabelleddatapetrinet.pnadapater;

import java.util.HashMap;
import java.util.Map;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataIndependent;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsEditable;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsImpl;

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
		
		Transition[] transitions = new Transition[sldpn.getNumberOfTransitions()];
		Place[] places = new Place[sldpn.getNumberOfPlaces()];
		
		for (int i = 0; i < sldpn.getNumberOfTransitions(); i++) {
			if (sldpn.isTransitionSilent(i)) {
				transitions[i] = pn.addTransition("tau"+i);
				transitions[i].setInvisible(true);
			} else {
				transitions[i] = pn.addTransition(sldpn.getTransitionLabel(i));
			}
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
	
	public static StochasticLabelledDataPetriNet viewAsSLDPN(AcceptingPetriNet acceptingPN) {
		
		StochasticLabelledPetriNetSimpleWeightsEditable slpn = new StochasticLabelledPetriNetSimpleWeightsImpl();

		Petrinet net = acceptingPN.getNet();
		
		Map<Place, Integer> placeIdx = new HashMap<>();
		Map<Transition, Integer> transitionIdx = new HashMap<>();
		
		for (Place p: net.getPlaces()) {
			int idx = slpn.addPlace();
			Integer tokens = acceptingPN.getInitialMarking().occurrences(p);
			if (tokens > 0) {
				slpn.addPlaceToInitialMarking(idx, tokens);
			}
			placeIdx.put(p, idx);
		}
		
		for (Transition t: net.getTransitions()) {
			int idx = slpn.addTransition(0);
			transitionIdx.put(t, idx);
			
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e: net.getOutEdges(t)) {
				slpn.addTransitionPlaceArc(idx, placeIdx.get(e.getTarget()));
			}
			
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e: net.getInEdges(t)) {
				slpn.addPlaceTransitionArc(placeIdx.get(e.getSource()), idx);
			}
			
			if (!t.isInvisible()) {
				slpn.setTransitionLabel(idx, t.getLabel());
			}
		}
		
		return new StochasticLabelledDataPetriNetWeightsDataIndependent(slpn);
	}

}
