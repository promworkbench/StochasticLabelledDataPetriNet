package org.processmining.stochasticlabelleddatapetrinet.example;

import java.util.List;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependentImpl;
import org.processmining.stochasticlabelleddatapetrinet.weights.ConstantWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.SimpleLinearWeightFunction;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsEditable;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsImpl;

public final class SLDPNExamples {
	
	private SLDPNExamples() {
		super();
	}

	public static StochasticLabelledDataPetriNet buildSimpleLinearWeightedChoiceSLDPN( ) {
		
		StochasticLabelledPetriNetSimpleWeightsEditable slpn = buildABCChoiceSLPN();
		
		// Add variable		
		List<String> varLabels = List.of("X");
		List<VariableType> varTypes = List.of(VariableType.CONTINUOUS);
		
		// Let A write and B and C read
		List<int[]> transRead = List.of(new int[] { }, new int[] { 0 }, new int[] { 0 });
		List<int[]> transWrite =  List.of(new int[] { 0 }, new int[] { }, new int[] { });
		
		StochasticLabelledDataPetriNetWeightsDataDependentImpl sldpn = new StochasticLabelledDataPetriNetWeightsDataDependentImpl(slpn, varLabels, varTypes, transRead, transWrite);
		
		assert sldpn.getTransitionLabel(0).equals("A");
		sldpn.setWeightFunction(0, new ConstantWeightFunction()); // A: weight = 1.0
		
		assert sldpn.getTransitionLabel(1).equals("B");
		sldpn.setWeightFunction(1, new SimpleLinearWeightFunction(10, 2, 0, 1)); // B: weight = 10 + 2X 
		
		assert sldpn.getTransitionLabel(2).equals("C");
		sldpn.setWeightFunction(2, new ConstantWeightFunction(10)); // C: weight = 1-			
		
		return sldpn;
	}


	private static StochasticLabelledPetriNetSimpleWeightsEditable buildABCChoiceSLPN() {
		// Simple XOR net
		StochasticLabelledPetriNetSimpleWeightsEditable slpn = new StochasticLabelledPetriNetSimpleWeightsImpl();
		
		int p1 = slpn.addPlace();
		slpn.addPlaceToInitialMarking(p1);
		
		int tA = slpn.addTransition("A", 1);
		slpn.addPlaceTransitionArc(p1, tA);
	
		int p2 = slpn.addPlace();		
		slpn.addTransitionPlaceArc(tA, p2);
		
		int tB = slpn.addTransition("B", p2);		
		int tC = slpn.addTransition("C", p2);
		
		slpn.addPlaceTransitionArc(p2, tB);
		slpn.addPlaceTransitionArc(p2, tC);
		
		int p3 = slpn.addPlace();
		
		slpn.addTransitionPlaceArc(tB, p3);
		slpn.addTransitionPlaceArc(tC, p3);
		return slpn;
	}

}
