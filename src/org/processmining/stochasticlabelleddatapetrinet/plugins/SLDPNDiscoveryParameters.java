package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.classification.XEventClassifier;

public interface SLDPNDiscoveryParameters {

	public XEventClassifier getClassifier();

	public boolean isOneHotEncoding();

	public int getOneHotEncodingMaximumPerVariable();
}