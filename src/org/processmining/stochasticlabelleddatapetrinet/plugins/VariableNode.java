package org.processmining.stochasticlabelleddatapetrinet.plugins;

import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.processmining.plugins.graphviz.dot.DotNode;

class VariableNode extends DotNode {
	
	public VariableNode(String label) {
		super(StringEscapeUtils.escapeXml11(label), Map.of("shape", "hexagon", 
				"fillcolor", "khaki1", "style", "filled", "fontsize", "8"));
	}
	
}