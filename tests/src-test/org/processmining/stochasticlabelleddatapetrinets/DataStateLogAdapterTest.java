package org.processmining.stochasticlabelleddatapetrinets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapterImpl;

public class DataStateLogAdapterTest {
	
	@Test
	public void testFromEvent() {
		StochasticLabelledDataPetriNet net = TestUtils.buildDataWeightTestModel();
		DataStateLogAdapter adapter = new DataStateLogAdapterImpl(net.getDefaultSemantics());
		
		DataState ds = adapter.fromEvent(TestUtils.buildTestLog().get(0).get(0));
		
		assertEquals(ds.getDouble(0), 10.0, 0.001);
	}

}
