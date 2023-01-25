package org.processmining.stochasticlabelleddatapetrinet.plugins;

import org.deckfour.xes.classification.XEventClassifier;

public abstract class SLDPNDiscoveryParametersAbstract implements SLDPNDiscoveryParameters {

	private XEventClassifier classifier;
	private boolean oneHotEncoding;
	private int oneHotEncodingMaximumPerVariable;

	public SLDPNDiscoveryParametersAbstract(XEventClassifier classifier, boolean oneHotEncoding,
			int oneHotEncodingMaximumPerVariable) {
		super();
		this.classifier = classifier;
		this.oneHotEncoding = oneHotEncoding;
		this.oneHotEncodingMaximumPerVariable = oneHotEncodingMaximumPerVariable;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public boolean isOneHotEncoding() {
		return oneHotEncoding;
	}

	public int getOneHotEncodingMaximumPerVariable() {
		return oneHotEncodingMaximumPerVariable;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public void setOneHotEncoding(boolean oneHotEncoding) {
		this.oneHotEncoding = oneHotEncoding;
	}

	public void setOneHotEncodingMaximumPerVariable(int oneHotEncodingMaximumPerVariable) {
		this.oneHotEncodingMaximumPerVariable = oneHotEncodingMaximumPerVariable;
	}

}