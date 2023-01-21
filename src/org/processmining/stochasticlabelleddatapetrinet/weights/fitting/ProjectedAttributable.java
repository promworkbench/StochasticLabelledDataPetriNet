package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import java.util.Set;

public interface ProjectedAttributable {
	
	Object getAttributeValue(String attributeName);

	Set<String> getAttributes();	

}