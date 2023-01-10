package org.processmining.stochasticlabelleddatapetrinet.logadapter;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public interface DataStateLogAdapter {
	
	DataState fromEvent(XEvent event);
	
	DataState fromTrace(XTrace trace);

}
