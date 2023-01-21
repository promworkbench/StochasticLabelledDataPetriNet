package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependent;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapterImpl;

public class LogisticRegressionWeightFitter {
	
	private final XEventClassifier classifier;

	public LogisticRegressionWeightFitter(XEventClassifier classifier) {
		super();
		this.classifier = classifier;
		//TODO use WEKA
	}

	public StochasticLabelledDataPetriNetWeights fit(XLog log, StochasticLabelledDataPetriNet sldpn) {
		
		StochasticLabelledDataPetriNetWeightsDataDependent sldpnWeights = new StochasticLabelledDataPetriNetWeightsDataDependent(sldpn);
		
		for (int tIdx = 0; tIdx < sldpnWeights.getNumberOfTransitions(); tIdx++) {											

			DataStateLogAdapter logAdapter = new DataStateLogAdapterImpl(sldpnWeights.getDefaultSemantics());
						
			for (XTrace t: log) {
				DataState ds = logAdapter.fromTrace(t);
				
				for (XEvent e: t) {
					ds = logAdapter.fromEvent(e, ds);
					
					if (classifier.getClassIdentity(e).equals(sldpn.getTransitionLabel(tIdx))) {
						
						// get positive example
						
					}
					
					
				}
			}
			
			
			double intercept;
			double[] coefficients;
			
			//sldpnWeights.setWeightFunction(tIdx, new LinearRegressionBasedWeightFunction(intercept, coefficients));	
		}
		
		return sldpnWeights;
	}

}
