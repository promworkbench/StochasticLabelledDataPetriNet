package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import java.util.Set;

public interface ProjectedLog extends Iterable<ProjectedTrace> {
	
	Set<String> getAttributes();
	
	Object getInitialValue(String attributeName);
	
}