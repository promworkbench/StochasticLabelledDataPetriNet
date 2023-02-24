package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

import com.google.common.collect.ImmutableMap;
import com.kitfox.svg.SVGDiagram;

public abstract class SLDPNVisualizer<T extends StochasticLabelledDataPetriNet> {
	
	private T net;
	private DotPanel dotPanel;
	private DataState dataState;
	private Map<Integer, DotNode> place2dotNode;
	private Map<Integer, DotNode> variable2dotNode;
	private Map<Integer, DotNode> transition2dotNode;
	private Dot dot;

	private final class ElementSelectionListenerColorImpl implements DotElementSelectionListener {

		private final DotPanel dotPanel;

		private String defaultStrokeWidth;
		private String defaultStrokeDashArray;

		private ElementSelectionListenerColorImpl(DotPanel dotPanel, DotElement node) {
			this.dotPanel = dotPanel;
			defaultStrokeWidth = DotPanel.getAttributeOf(DotPanel.getSVGElementOf(dotPanel.getSVG(), node),
					"stroke-width");
			defaultStrokeDashArray = DotPanel.getAttributeOf(DotPanel.getSVGElementOf(dotPanel.getSVG(), node),
					"stroke-dasharray");
		}

		public void colorSelectedElement(final DotPanel dotPanel, DotElement element) {
			defaultStrokeWidth = DotPanel.setCSSAttributeOf(dotPanel.getSVG(), element, "stroke-width", "3");
			defaultStrokeDashArray = DotPanel.setCSSAttributeOf(dotPanel.getSVG(), element, "stroke-dasharray", "5,5");
		}

		public void selected(DotElement element, SVGDiagram image) {
			colorSelectedElement(dotPanel, element);
			dotPanel.repaint();
		}

		public void deselected(DotElement element, SVGDiagram image) {
			DotPanel.setCSSAttributeOf(dotPanel.getSVG(), element, "stroke-width", defaultStrokeWidth);
			DotPanel.setCSSAttributeOf(dotPanel.getSVG(), element, "stroke-dasharray", defaultStrokeDashArray);
			dotPanel.repaint();
		}
	}	
	
	public SLDPNVisualizer(T net) {
		super();
		this.net = net;
		this.dataState = net.getDefaultSemantics().newDataState();
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
			VariableNode dotNode = new VariableNode(net.getVariableLabel(variable));
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
		
		updateVariablesForState();
		updateTransitionsForState();	
		
		dotPanel = new DotPanel(dot);
		
		for (final DotNode node : dotPanel.getNodes()) {
			node.addSelectionListener(new ElementSelectionListenerColorImpl(dotPanel, node));
		}
		
		for (final Entry<Integer, DotNode> entry: variable2dotNode.entrySet()) {
			DotNode dotNode = entry.getValue();
			final JPopupMenu menu = new JPopupMenu();
			menu.add(new JMenuItem(new AbstractAction("Assign value") {

				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					String userInput = JOptionPane.showInputDialog(dotPanel,
							"Enter value in format #.#", Double.toString(0.0));
					if (userInput == null || userInput.isEmpty()) {
						JOptionPane.showConfirmDialog(dotPanel,
								"Please use format '#.##' (for example, 0.05)", "Invalid format",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					} else {
						try {
							setVariableValue(entry.getKey(), Double.parseDouble(userInput));
						} catch (NumberFormatException e1) {
							JOptionPane.showConfirmDialog(dotPanel,
									"Please use format '#.##' (for example, 0.05). Error " + e.toString(),
									"Invalid format", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
						}
					}
				}

			}));

			dotNode.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()) {
						showMenu(e);
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger()) {
						showMenu(e);
					}
				}

				private void showMenu(MouseEvent e) {
					menu.show(e.getComponent(), e.getX(), e.getY());
				}

			});

		}
		
		return dotPanel;
	}

	public void decoratePlace(T net, int place, DotNode dotNode) {
	}

	public void decorateTransition(T net, int transition, DotNode dotNode) {
		dotNode.setSelectable(true);
	}

	public void decorateVariable(T net, int variable, DotNode dotNode) {
		dotNode.setSelectable(true);
	}

	protected void setVariableValue(int variable, double val) {
		if (net.getVariableType(variable) == VariableType.CONTINUOUS) {
			dataState.putDouble(variable, val);	
		} else {
			dataState.putLong(variable, (long) val);
		}
		
		updateVariablesForState();
		updateTransitionsForState();

		dotPanel.changeDot(dot, false);
	}

	private void updateVariablesForState() {
		for(Entry<Integer, DotNode> entry: variable2dotNode.entrySet()) {
			Integer idx = entry.getKey();
			if (dataState.contains(idx)) {
				if (net.getVariableType(idx) == VariableType.CONTINUOUS) {
					entry.getValue().setOption("xlabel", String.format("%.2f", dataState.getDouble(idx)));
				} else {
					entry.getValue().setOption("xlabel", String.format("%d", dataState.getLong(idx)));
				}				
			} else {
				entry.getValue().setOption("xlabel", "NA");
			}
		}
	}

	private void updateTransitionsForState() {
		for (Entry<Integer, DotNode> entry: transition2dotNode.entrySet()) {
			double weight = getTransitionWeight(entry.getKey(), dataState);
			Color color = ColorScheme.BLUE_SINGLE_HUE.getColorFromGradient(weight);
			entry.getValue().setOption("fillcolor", convertColor(color));
			entry.getValue().setOption("fontcolor", convertColor(determineFontColor(null, color)));
			entry.getValue().setOption("xlabel", String.format("<<font color=\"black\">%.2f</font>>", weight));
		}
	}
	
	private static String convertColor(Color color) {
		return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
	}
	
	private static final Color determineFontColor(final Color textColor, final Color bgColor) {
		if (textColor == null) {
			double val = Math.sqrt(.299 * Math.pow(bgColor.getRed(), 2) + .587 * Math.pow(bgColor.getGreen(), 2)
					+ .114 * Math.pow(bgColor.getBlue(), 2));
			return (val < 130) ? Color.WHITE : Color.BLACK;
		} else {
			return textColor;
		}
	}

	protected abstract double getTransitionWeight(int a, DataState dataState);
		

}