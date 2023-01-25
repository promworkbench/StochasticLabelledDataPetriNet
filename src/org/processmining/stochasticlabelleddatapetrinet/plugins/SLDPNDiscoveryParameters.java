package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.classification.XEventClassifier;

public interface SLDPNDiscoveryParameters {

	public XEventClassifier getClassifier();

	public boolean isOneHotEncoding();

	/**
	 * -1 indicates that no limit should be applied
	 * 
	 * @return
	 */
	public int getOneHotEncodingMaximumPerVariable();
}