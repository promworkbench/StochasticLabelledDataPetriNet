package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;

public class SLDPNDiscoveryParametersDefault extends SLDPNDiscoveryParametersAbstract {

	public final static boolean default_oneHotEncoding = true;
	public final static int default_oneHotEncodingMaximumPerVariable = 100;
	public final static XEventClassifier default_classifier = MiningParameters.getDefaultClassifier();

	public SLDPNDiscoveryParametersDefault() {
		super(default_classifier, default_oneHotEncoding, default_oneHotEncodingMaximumPerVariable);
	}

}