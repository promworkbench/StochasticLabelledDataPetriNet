package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import java.util.Set;

public interface ProjectedTrace extends Iterable<ProjectedEvent> {
	Object getAttributeValue(String attributeName);

	Set<String> getAttributes();
}