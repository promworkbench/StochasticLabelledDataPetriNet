package org.processmining.stochasticlabelleddatapetrinets;

import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XLogBuilder;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependent;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataIndependent;
import org.processmining.stochasticlabelleddatapetrinet.weights.ConstantWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.DirectDataWeightFunction;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsEditable;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsImpl;

public class TestUtils {

	static XLog buildTestLog() {
		final XLog log = XLogBuilder.newInstance()
				.startLog("test")
				.addTrace("t1", 10)
					.addEvent("A").addAttribute("X", 10.0)
					.addEvent("B")
				.addTrace("t2", 20)
					.addEvent("A").addAttribute("X", 5.0)
					.addEvent("C").build();	
		return log;
	}

	static StochasticLabelledPetriNetSimpleWeightsEditable buildSLPN() {
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

	static StochasticLabelledDataPetriNet buildConstantWeightTestModel() {		
		StochasticLabelledPetriNetSimpleWeightsEditable slpn = buildSLPN();		
		return new StochasticLabelledDataPetriNetWeightsDataIndependent(slpn);
	}

	static StochasticLabelledDataPetriNet buildDataWeightTestModel() {
		
		StochasticLabelledPetriNetSimpleWeightsEditable slpn = buildSLPN();
		
		// Add variable		
		List<String> varLabels = List.of("X");
		List<VariableType> varTypes = List.of(VariableType.CONTINUOUS);
		List<int[]> transRead = List.of(new int[] { }, new int[] { 0 }, new int[] { 0 });
		List<int[]> transWrite =  List.of(new int[] { 0 }, new int[] { }, new int[] { });
		
		StochasticLabelledDataPetriNetWeightsDataDependent sldpn = new StochasticLabelledDataPetriNetWeightsDataDependent(slpn, varLabels, varTypes, transRead, transWrite);
		
		assert sldpn.getTransitionLabel(0).equals("A");
		sldpn.setWeightFunction(0, new ConstantWeightFunction()); // A
		
		assert sldpn.getTransitionLabel(1).equals("B");
		sldpn.setWeightFunction(1, new DirectDataWeightFunction(0)); // B
		
		assert sldpn.getTransitionLabel(2).equals("C");
		sldpn.setWeightFunction(2, new DirectDataWeightFunction(0)); // B		
		
		return sldpn;
	}

}
