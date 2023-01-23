package org.processmining.stochasticlabelleddatapetrinets;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.processmining.stochasticlabelleddatapetrinet.example.SLDPNExamples;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.SLDPNReplayer;

public class SLDPNSemanticsTest {
	
	@Test
	public void testConstantWeightReplay() {
		SLDPNReplayer replayer = new SLDPNReplayer();
		assertTrue(replayer.replay(SimpleTestLog.buildConstantWeightTestModel(), SimpleTestLog.buildTestLog().get(0)) > 0);
	}
	
	@Test
	public void testDataWeightReplayWithB() {
		SLDPNReplayer replayer = new SLDPNReplayer();				
		assertTrue(replayer.replay(SimpleTestLog.buildDataWeightTestModel(), SimpleTestLog.buildTestLog().get(0)) == 11);
	}
	
	@Test
	public void testDataWeightReplayWithC() {
		SLDPNReplayer replayer = new SLDPNReplayer();				
		assertTrue(replayer.replay(SimpleTestLog.buildDataWeightTestModel(), SimpleTestLog.buildTestLog().get(10)) == 6);
	}
	
	@Test
	public void testSLDPNExamplesSimple() {
		SLDPNReplayer replayer = new SLDPNReplayer();				
		assertTrue(replayer.replay(SLDPNExamples.buildSimpleLinearWeightedChoiceSLDPN(), SimpleTestLog.buildTestLog().get(0)) == 31);
	}	


}
