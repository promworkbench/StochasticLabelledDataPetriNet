package org.processmining.stochasticlabelleddatapetrinets.logadapter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapterImpl;
import org.processmining.stochasticlabelleddatapetrinets.SimpleTestLog;

public class DataStateLogAdapterTest {
	
	@Test
	public void testFromEvent() {
		StochasticLabelledDataPetriNet net = SimpleTestLog.buildDataWeightTestModel();
		DataStateLogAdapter adapter = new DataStateLogAdapterImpl(net.getDefaultSemantics());
		
		DataState ds = adapter.fromEvent(SimpleTestLog.buildTestLog().get(0).get(0));
		
		assertEquals(ds.getDouble(0), 10.0, 0.001);
	}

}
