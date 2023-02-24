package org.processmining.stochasticlabelleddatapetrinets;

import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XLogBuilder;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependent;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataIndependent;
import org.processmining.stochasticlabelleddatapetrinet.weights.ConstantWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.DirectDataWeightFunction;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsEditable;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsImpl;

import com.google.common.collect.ImmutableList;

public class SimpleTestLog {

	public static XLog buildTestLog() {
		final XLog log = XLogBuilder.newInstance()
				.startLog("test")
				.addTrace("t1", 10)
					.addEvent("A")
						.addAttribute("X", 10.0)
					.addEvent("B")
				.addTrace("t2", 20)
					.addEvent("A")
						.addAttribute("X", 5.0)
				.addEvent("C").build();
		return log;
	}

	public static XLog buildTestLog2Variables() {
		final XLog log = XLogBuilder.newInstance().startLog("test")
				.addTrace("t1", 10)
					.addEvent("A")
						.addAttribute("X", 10.0)
						.addAttribute("Y", -10.0)
					.addEvent("B").addTrace("t2", 20)
						.addEvent("A")
						.addAttribute("X", 5.0)
						.addAttribute("Y", -5.0)
					.addEvent("C").build();
		return log;
	}
	
	public static XLog buildTestCaseVarsLog() {
		final XLog log = XLogBuilder.newInstance()
				.startLog("test")
				.addTrace("t1", 1000)
					.addAttribute("X", 10.0)
					.addEvent("A")
					.addEvent("B")
				.addTrace("t2", 2000)
					.addAttribute("X", 5.0)
					.addEvent("A")
					.addEvent("C").build();
		return log;
	}
	
	public static XLog buildTestABCaseVarsLog() {
		final XLog log = XLogBuilder.newInstance()
				.startLog("test")
				.addTrace("t1", 1000)
					.addAttribute("X", 10.0)
					.addEvent("A")
				.addTrace("t2", 2000)
					.addAttribute("X", 5.0)
					.addEvent("B").build();
		return log;
	}	
	
	public static XLog buildTestLog2TraceVariables() {
		final XLog log = XLogBuilder.newInstance().startLog("test")//
				.addTrace("t1", 5000).addAttribute("X", 10).addEvent("A")//
				.addTrace("t1", 1000).addAttribute("X", 5).addEvent("B")//
				.build();
		return log;
	}
	
	private static StochasticLabelledPetriNetSimpleWeightsEditable build_AB_SLPN() {
		// Simple XOR net
		StochasticLabelledPetriNetSimpleWeightsEditable slpn = new StochasticLabelledPetriNetSimpleWeightsImpl();

		int p1 = slpn.addPlace();
		slpn.addPlaceToInitialMarking(p1);

		int tA = slpn.addTransition("A", 1);
		slpn.addPlaceTransitionArc(p1, tA);

		int tB = slpn.addTransition("B", 1);
		slpn.addPlaceTransitionArc(p1, tB);

		int p2 = slpn.addPlace();
		slpn.addTransitionPlaceArc(tA, p2);
		slpn.addTransitionPlaceArc(tB, p2);
		return slpn;
	}

	public static StochasticLabelledDataPetriNetWeightsDataIndependent buildABModel() {
		StochasticLabelledPetriNetSimpleWeightsEditable slpn = build_AB_SLPN();
		return new StochasticLabelledDataPetriNetWeightsDataIndependent(slpn);
	}
	
	private static StochasticLabelledPetriNetSimpleWeightsEditable buildSLPN() {
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

	public static StochasticLabelledDataPetriNetWeightsDataIndependent buildConstantWeightTestModel() {
		StochasticLabelledPetriNetSimpleWeightsEditable slpn = buildSLPN();
		return new StochasticLabelledDataPetriNetWeightsDataIndependent(slpn);
	}

	public static StochasticLabelledDataPetriNetWeightsDataDependent buildDataWeightTestModel() {

		StochasticLabelledPetriNetSimpleWeightsEditable slpn = buildSLPN();

		// Add variable		
		List<String> varLabels = ImmutableList.of("X");
		List<VariableType> varTypes = ImmutableList.of(VariableType.CONTINUOUS);
		List<int[]> transRead = ImmutableList.of(new int[] {}, new int[] { 0 }, new int[] { 0 });
		List<int[]> transWrite = ImmutableList.of(new int[] { 0 }, new int[] {}, new int[] {});

		StochasticLabelledDataPetriNetWeightsDataDependent sldpn = new StochasticLabelledDataPetriNetWeightsDataDependent(
				slpn, varLabels, varTypes, transRead, transWrite);

		assert sldpn.getTransitionLabel(0).equals("A");
		sldpn.setWeightFunction(0, new ConstantWeightFunction());

		assert sldpn.getTransitionLabel(1).equals("B");
		sldpn.setWeightFunction(1, new DirectDataWeightFunction(0));

		assert sldpn.getTransitionLabel(2).equals("C");
		sldpn.setWeightFunction(2, new DirectDataWeightFunction(0));

		return sldpn;
	}

	public static StochasticLabelledDataPetriNetWeightsDataDependent buildDataWeight2VariablesTestModel() {

		StochasticLabelledPetriNetSimpleWeightsEditable slpn = buildSLPN();

		// Add variable		
		List<String> varLabels = ImmutableList.of("X", "Y");
		List<VariableType> varTypes = ImmutableList.of(VariableType.CONTINUOUS, VariableType.CONTINUOUS);
		List<int[]> transRead = ImmutableList.of(new int[] {}, new int[] { 0, 1 }, new int[] { 0, 1 });
		List<int[]> transWrite = ImmutableList.of(new int[] { 0, 1 }, new int[] {}, new int[] {});

		StochasticLabelledDataPetriNetWeightsDataDependent sldpn = new StochasticLabelledDataPetriNetWeightsDataDependent(
				slpn, varLabels, varTypes, transRead, transWrite);

		assert sldpn.getTransitionLabel(0).equals("A");
		sldpn.setWeightFunction(0, new ConstantWeightFunction());

		assert sldpn.getTransitionLabel(1).equals("B");
		sldpn.setWeightFunction(1, new ConstantWeightFunction());

		assert sldpn.getTransitionLabel(2).equals("C");
		sldpn.setWeightFunction(2, new ConstantWeightFunction());

		return sldpn;
	}

}
