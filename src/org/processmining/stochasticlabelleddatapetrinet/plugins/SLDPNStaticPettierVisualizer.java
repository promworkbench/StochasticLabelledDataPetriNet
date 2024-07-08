package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataStateFactoryImpl;
import org.processmining.stochasticlabelleddatapetrinet.weights.ConstantWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.LogisticWeightFunction;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class SLDPNStaticPettierVisualizer {
	
	private SLDPNStaticPettierVisualizer() {};
	
	public static final String PlaceFill = "#f2f2f2";
	public static final String StartingPlaceFill = "#80ff00";
	public static final String EndingPlaceFill = "#FF3939";
	public static final String TransitionFill = "#e9c6af";
	public static final String TauFill = "#808080";
	public static final String WeightFill = "#c0bbbb";
	
	public static final DotPanel visualise(
			SLDPN net) {
		Dot dot = new Dot();
		StochasticLabelledDataPetriNetWeights model = net.getModel();

		dot.setOption("forcelabels", "true");
		dot.setOption("bgcolor", "none");

		TIntObjectMap<DotNode> place2dotNode = new TIntObjectHashMap<>(10, 0.5f, -1);
		
		
		for (int place=0; place < model.getNumberOfPlaces() ;place++) {
			DotNode dotNode = dot.addNode("");
			dotNode.setOption("shape", "circle");
			dotNode.setOption("style", "filled");
			dotNode.setOption("fillcolor", PlaceFill);
			place2dotNode.put(place, dotNode);

			if (model.isInInitialMarking(place) > 0) {
				dotNode.setOption("fillcolor", StartingPlaceFill);
			}
			
			if (model.getOutputTransitions(place).length == 0){
				dotNode.setOption("fillcolor", EndingPlaceFill);
			}
		}
		
		int tau = 0;
		DecimalFormat df = new DecimalFormat("0.000");
		df.setMaximumFractionDigits(3);
		df.setMinimumFractionDigits(3);
		String intercept = "";
		
		for (int trans=0; trans < model.getNumberOfTransitions(); trans++) {
			DotNode dotNode;
			
			LogisticWeightFunction func = null;
			ConstantWeightFunction func2 = null;
			System.out.println(model.getWeightFunction(trans).getClass());
			if (model.getWeightFunction(trans) instanceof LogisticWeightFunction) {
				func = (LogisticWeightFunction) model.getWeightFunction(trans);
				intercept = df.format(
						func.getIntercept()
				);
				
			} else if (model.getWeightFunction(trans) instanceof ConstantWeightFunction) {
				func2 = (ConstantWeightFunction) model.getWeightFunction(trans);
				intercept = df.format(
						func2.evaluateWeight(model, 
						new DataStateFactoryImpl(0).newDataState()
						)
				);
			}
			String headLabel = "";
			if (model.isTransitionSilent(trans)) {
				tau+= 1;
				headLabel = "<"
						+ "<TABLE"
						+ " BORDER=\"0\" "
						+ "><TR>"
						+ "<TD COLSPAN=\"3\">"
						+ "<FONT POINT-SIZE=\"16\" >"
						+ "&#120591;"
						+ "</FONT>"
						+ "<FONT POINT-SIZE=\"10\">(" 
						+ tau 
						+")</FONT></TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD ALIGN=\"LEFT\" COLSPAN=\"3\">"
						+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
						+ "<I>Intercept:</I>"
						+ "</FONT>"
						+ "</TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD BORDER=\"1\" BGCOLOR=\"#c0bbbb\" "
						+ "STYLE=\"ROUNDED,DASHED\" "
						+ "CELLPADDING=\"5\" COLSPAN=\"3\" "
						+ ">"
						+ intercept
						+ "</TD>"
						+ "</TR>";
			} else {
				headLabel = "<"
						+ "<TABLE"
						+ " BORDER=\"0\" "
						+ "><TR>"
						+ "<TD COLSPAN=\"3\">" 
						+ model.getTransitionLabel(trans)
						+"</TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD ALIGN=\"LEFT\" COLSPAN=\"3\">"
						+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
						+ "<I>Intercept:</I>"
						+ "</FONT>"
						+ "</TD>"
						+ "</TR>"
						+ "<TR>"
						+ "<TD BORDER=\"1\" BGCOLOR=\"#c0bbbb\" "
						+ "STYLE=\"ROUNDED,DASHED\" "
						+ "CELLPADDING=\"5\" COLSPAN=\"3\" "
						+ ">"
						+ intercept
						+ "</TD>"
						+ "</TR>";
			}
				
			StringBuilder coefficients = new StringBuilder(headLabel);
			Map<String,Integer> LabelsToPos = new HashMap<>();
			List<String> vars = new ArrayList<>();
			for ( int coef=0; coef < model.getNumberOfVariables(); coef++) {
				String label = model.getVariableLabel(coef);
				LabelsToPos.put(label, coef);
				if (label.contains("_")) {
					label = label.substring(0,label.indexOf("_"));
					if (!vars.contains(label)) {
						vars.add(label);
					}
				} else {
					if (!vars.contains(label)) {
						vars.add(label);
					}
					
				}
			}
			vars.sort(String::compareTo);
			if (func != null) {
				coefficients.append(""
					+ "<TR>"
					+ "<TD ALIGN=\"LEFT\" COLSPAN=\"3\">"
					+ "<FONT ALIGN=\"LEFT\" POINT-SIZE=\"10\" >"
					+ "<I>Coefficients:</I>"
					+ "</FONT>"
					+ "</TD>"
					+ "</TR>");
				double[] coeficientNumbers = func.getCoefficients();
				for (String varLabel : vars) {
					if (LabelsToPos.containsKey(varLabel)) {
						int coef = LabelsToPos.get(varLabel);
						if (coeficientNumbers[coef] != 0) {
							coefficients.append("<TR>"
									+ "<TD BGCOLOR=\"#80ff00\" "
									+ "STYLE=\"ROUNDED\" "
									+ "CELLPADDING=\"3\" "
									+ ">"
									+ " + "
									+ "</TD>"
									+ "<TD>"
									+ model.getVariableLabel(coef)
									+ "</TD>"
									+ "<TD>"
									+ " * "
									+ df.format(coeficientNumbers[coef])
									+ "</TD>"
									+ "</TR>"
									);
						}
					} else {
						Set<String> varLabels = new HashSet<>();
						varLabels.addAll(LabelsToPos.keySet());		
						varLabels.removeIf(
								(s) -> !s.contains(varLabel)
						);
						System.out.println(varLabels.toString());
						Map<Object, String> mapping = net.getOneHotEncoding()
								.getMapping(varLabel);
						Map<String, Double> localNumbers = new HashMap();
						if (mapping!= null) {
							System.out.println(mapping.toString());
							for(String posVarLabel: varLabels) {
								for(Entry<Object, String> entry : mapping.entrySet()) {
									if (entry.getValue().equals(posVarLabel)) {
										String encode = (String) entry.getKey();
										double coefNumber = coeficientNumbers[
										       LabelsToPos.get(posVarLabel)];
										if (coefNumber != 0) {
											localNumbers.put(encode, coefNumber);
										}
									
									}
								}
								
							}
							System.out.println(localNumbers.toString());
						}
						
						if (localNumbers.keySet().size() > 0) {
							coefficients.append(""
									+ "<TR>"
									+ "<TD "
									+ "ROWSPAN=\""
									+ localNumbers.keySet().size()
									+ "\" "
									+ "STYLE=\"ROUNDED,DASHED\" "
									+ "BGCOLOR=\"#FF3939\" "
									+ "CELLPADDING=\"3\" "
									+ "BORDER=\"1\" "
									+ ""
									+ ">"
									+ varLabel
									+ ""
									+ "</TD>"
									+ "<TD "
									+ "ROWSPAN=\""
									+ localNumbers.keySet().size()
									+ "\" "
									+ "BGCOLOR=\"#80ff00\" "
									+ "STYLE=\"ROUNDED\" "
									+ "CELLSPACING=\"8\" "
									+ ">"
									+ "+"
									+ "</TD>");
							for (Entry<String, Double> entry : localNumbers.entrySet()) {
								coefficients.append(""
										+ "<TD>"
										+ entry.getKey()
										+ "&rarr;"
										+ df.format(entry.getValue())
										+ "</TD>"
										+ "</TR>");
								coefficients.append("<TR>");
							}
							coefficients = new StringBuilder(
									coefficients.substring(0, coefficients.length()-4)
							);
//							coefficients.append("<TD></TD></TR>");
						}
					}
				}
			}
				
			coefficients.append("</TABLE>"
					+ ">");
			dotNode = dot.addNode(coefficients.toString());
			
			if (model.isTransitionSilent(trans)) {
				dotNode.setOption("style", "filled,rounded");
				dotNode.setOption("fillcolor", TauFill);
			} else {
				dotNode.setOption("style", "rounded,filled");
				dotNode.setOption("fillcolor", TransitionFill);
			}
			
			dotNode.setOption("shape", "box");
			dotNode.setOption("width", "1");
			
			for (int outplace : model.getOutputPlaces(trans)) {
					dot.addEdge(dotNode, 
						place2dotNode.get(outplace)
					);
			}
			for (int inplace : model.getInputPlaces(trans)) {
					dot.addEdge(
						place2dotNode.get(inplace),
						dotNode
					);
			}
		}

		return new DotPanel(dot);
	}

}
