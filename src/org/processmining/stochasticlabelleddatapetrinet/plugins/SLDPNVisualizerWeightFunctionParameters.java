package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.util.HashMap;
import java.util.Map;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;

import com.google.common.collect.ImmutableMap;

public abstract class SLDPNVisualizerWeightFunctionParameters<T extends StochasticLabelledDataPetriNet> {
	
	private T net;
	private DotPanel dotPanel;
	private Map<Integer, DotNode> place2dotNode;
	private Map<Integer, DotNode> variable2dotNode;
	private Map<Integer, DotNode> transition2dotNode;
	private Dot dot;

	public SLDPNVisualizerWeightFunctionParameters(T net) {
		super();
		this.net = net;
	}

	public DotPanel visualiseNet() {
		dot = new Dot();

		dot.setOption("forcelabels", "true");

		place2dotNode = new HashMap<>();

		for (int place = 0; place < net.getNumberOfPlaces(); place++) {
			DotNode dotNode = dot.addNode("");
			dotNode.setOption("shape", "circle");
			place2dotNode.put(place, dotNode);

			if (net.isInInitialMarking(place) > 0) {
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "#80ff00");
			}

			decoratePlace(net, place, dotNode);
		}
		
		variable2dotNode = new HashMap<>();
		
		for (int variable = 0; variable < net.getNumberOfVariables(); variable++) {
			VariableNode dotNode = new VariableNode(net.getVariableLabel(variable) + "(" + variable +")");
			variable2dotNode.put(variable, dotNode);
			dot.addNode(dotNode);
		}		

		transition2dotNode = new HashMap<>();
		
		for (int transition = 0; transition < net.getNumberOfTransitions(); transition++) {
			DotNode dotNode;

			if (net.isTransitionSilent(transition)) {
				dotNode = dot.addNode("" + transition);
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "#8EBAE5");
			} else {
				dotNode = dot.addNode(net.getTransitionLabel(transition));
				dotNode.setOption("style", "filled");
				dotNode.setOption("fillcolor", "#FFFFFF");
			}

			dotNode.setOption("shape", "box");

			decorateTransition(net, transition, dotNode);

			for (int place : net.getOutputPlaces(transition)) {
				dot.addEdge(dotNode, place2dotNode.get(place));
			}

			for (int place : net.getInputPlaces(transition)) {
				dot.addEdge(place2dotNode.get(place), dotNode);
			}
			
			for (int variable: net.getWriteVariables(transition)) {
				dot.addEdge(dotNode, variable2dotNode.get(variable), "", 
						ImmutableMap.of("style", "invis", "penwidth", "2"));
			}
			
			transition2dotNode.put(transition, dotNode);
		}
		
		dotPanel = new DotPanel(dot);
		
		return dotPanel;
	}

	public void decoratePlace(T net, int place, DotNode dotNode) {
	}

	public void decorateTransition(T net, int transition, DotNode dotNode) {
		dotNode.setOption("xlabel", getWeightFunctionParameters(transition));
	}

	public void decorateVariable(T net, int variable, DotNode dotNode) {
	}

	protected abstract String getWeightFunctionParameters(int a);

}