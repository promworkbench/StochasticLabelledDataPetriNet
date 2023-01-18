package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import java.util.Set;

public interface ProjectedEvent {
	
	Object getActivity();

	Object getAttributeValue(String attributeName);

	Set<String> getAttributes();
}