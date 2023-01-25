package org.processmining.stochasticlabelleddatapetrinet.logadapter;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
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
		return fromAttributable(semantics.newDataState(), event);		
	}

	public DataState fromTrace(XTrace trace) {
		return fromAttributable(semantics.newDataState(), trace);
	}

	public DataState fromEvent(XEvent event, DataState ds) {
		return fromAttributable(ds, event);
	}

	public DataState fromTrace(XTrace trace, DataState ds) {
		return fromAttributable(ds, trace);
	}
	
	private DataState fromAttributable(DataState ds, XAttributable attr) {
						
		XAttributeMap attributes = attr.getAttributes();
	
		for (int i = 0; i < semantics.getNumberOfVariables(); i++) {
			String varLabel = semantics.getVariableLabel(i);

			if (attributes.containsKey(varLabel)) {
				VariableType varType = semantics.getVariableType(i);
				switch (varType) {
					case CONTINUOUS:
						if (!(attributes.get(varLabel) instanceof XAttributeContinuous)) {
							throw new RuntimeException("Invalid attribute type mapped to CONTINUOUS variable!");
						}
						ds.putDouble(i, (Double) XUtils.getAttributeValue(attributes.get(varLabel)));
						break;
					case DISCRETE:
					case CATEGORICAL:
						if (!(attributes.get(varLabel) instanceof XAttributeDiscrete)) {
							throw new RuntimeException("Invalid attribute type mapped to DISCRETE or CATEGORICAL variable!");
						}
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
