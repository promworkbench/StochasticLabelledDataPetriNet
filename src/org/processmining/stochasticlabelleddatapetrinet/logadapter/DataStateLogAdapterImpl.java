package org.processmining.stochasticlabelleddatapetrinet.logadapter;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XUtils;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class DataStateLogAdapterImpl implements DataStateLogAdapter {
		
	private final StochasticLabelledDataPetriNetSemantics semantics;

	public DataStateLogAdapterImpl(StochasticLabelledDataPetriNetSemantics semantics) {
		super();
		this.semantics = semantics;
	}

	public DataState fromEvent(XEvent event) {				
		return fromAttributable(event);		
	}

	public DataState fromTrace(XTrace trace) {
		return fromAttributable(trace);
	}
	
	private DataState fromAttributable(XAttributable attr) {
						
		XAttributeMap attributes = attr.getAttributes();
		
		DataState ds = semantics.newDataState();
		
		for (int i = 0; i < semantics.getNumberOfVariables(); i++) {
			String varLabel = semantics.getVariableLabel(i);

			if (attributes.containsKey(varLabel)) {
				VariableType varType = semantics.getVariableType(i);
				switch (varType) {
					case CONTINUOUS:
						ds.putDouble(i, (Double) XUtils.getAttributeValue(attributes.get(varLabel)));
						break;
					case DISCRETE:
					case CATEGORICAL:
						ds.putLong(i, (Long) XUtils.getAttributeValue(attributes.get(varLabel)));
						break; //TODO handle string variables and variable mapping
					default :
						break;
				}				
			} // variable remains not set			
		}
		
		return ds;		
	}

}
