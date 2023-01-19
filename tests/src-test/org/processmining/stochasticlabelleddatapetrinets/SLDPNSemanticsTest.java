package org.processmining.stochasticlabelleddatapetrinets;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.SLDPNReplayer;

public class SLDPNSemanticsTest {
	
	@Test
	public void testConstantWeightReplay() {
		SLDPNReplayer replayer = new SLDPNReplayer();
		assertTrue(replayer.replay(TestUtils.buildConstantWeightTestModel(), TestUtils.buildTestLog().get(0)) > 0);
	}
	
	@Test
	public void testDataWeightReplayWithB() {
		SLDPNReplayer replayer = new SLDPNReplayer();				
		assertTrue(replayer.replay(TestUtils.buildDataWeightTestModel(), TestUtils.buildTestLog().get(0)) == 11);
	}
	
	@Test
	public void testDataWeightReplayWithC() {
		SLDPNReplayer replayer = new SLDPNReplayer();				
		assertTrue(replayer.replay(TestUtils.buildDataWeightTestModel(), TestUtils.buildTestLog().get(10)) == 6);
	}	

}
