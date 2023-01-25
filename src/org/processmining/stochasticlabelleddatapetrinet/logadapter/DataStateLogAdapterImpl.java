package org.processmining.stochasticlabelleddatapetrinet.logadapter;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
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

			XAttribute a = attributes.get(varLabel);
			if (a != null) {
				VariableType varType = semantics.getVariableType(i);
				switch (varType) {
					case CONTINUOUS:
						if (a instanceof XAttributeDiscrete) {
							ds.putDouble(i, ((Long) XUtils.getAttributeValue(a)).doubleValue());
						} else {
							if (!(attributes.get(varLabel) instanceof XAttributeContinuous)) {
								throw new RuntimeException("Invalid attribute type mapped to CONTINUOUS variable! Variable " + varLabel);
							}
							ds.putDouble(i, (Double) XUtils.getAttributeValue(a));							
						}
						break;
					case DISCRETE:
						if (!(a instanceof XAttributeDiscrete)) {
							throw new RuntimeException("Invalid attribute type mapped to DISCRETE or CATEGORICAL variable! Variable " + varLabel);
						}
						ds.putLong(i, (Long) XUtils.getAttributeValue(a));
						break; 
					case CATEGORICAL:
						//TODO handle string variables and variable mapping
						throw new RuntimeException("Categorical attributes are not yet supported! Variable " + varLabel);
					default :
						break;
				}				
			} // variable remains not set			
		}
		
		return ds;		
	}

}
