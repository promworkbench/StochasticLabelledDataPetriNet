package org.processmining.stochasticlabelleddatapetrinet.logadapter;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public interface DataStateLogAdapter {

	/**
	 * Reads variables from attributes into a new DataState.
	 * 
	 * @param event
	 * @return
	 */
	DataState fromEvent(XEvent event);

	/**
	 * Reads variables from attributes into an existing DataState updating
	 * variables that can be mapped to an attribute with new values and leaving
	 * all others as they are.
	 * 
	 * @param event
	 * @param ds
	 * @return
	 */
	DataState fromEvent(XEvent event, DataState ds);

	/**
	 * Reads variables from attributes into a new DataState.
	 * 
	 * @param trace
	 * @return
	 */
	DataState fromTrace(XTrace trace);

	/**
	 * 
	 * Reads variables from attributes into an existing DataState updating
	 * variables that can be mapped to an attribute with new values and leaving
	 * all others as they are
	 * 
	 * @param trace
	 * @param ds
	 * @return
	 */
	DataState fromTrace(XTrace trace, DataState ds);

}