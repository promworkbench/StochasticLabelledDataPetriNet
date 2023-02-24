package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.apache.commons.text.StringEscapeUtils;
import org.processmining.plugins.graphviz.dot.DotNode;

import com.google.common.collect.ImmutableMap;

class VariableNode extends DotNode {
	
	public VariableNode(String label) {
		super(StringEscapeUtils.escapeXml11(label), ImmutableMap.of("shape", "hexagon", 
				"fillcolor", "khaki1", "style", "filled", "fontsize", "8"));
	}
	
}