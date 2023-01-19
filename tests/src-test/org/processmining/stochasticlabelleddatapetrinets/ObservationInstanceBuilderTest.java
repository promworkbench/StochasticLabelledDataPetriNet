package org.processmining.stochasticlabelleddatapetrinets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.datapetrinets.DataPetriNet.PetrinetWithMarkings;
import org.processmining.log.utils.XUtils;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.balancedconformance.BalancedDataXAlignmentPlugin;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.controlflow.ControlFlowAlignmentException;
import org.processmining.plugins.balancedconformance.dataflow.exception.DataAlignmentException;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetUtils;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.ObservationInstanceBuilder;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.ProjectedLog;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.SetMultimap;

public class ObservationInstanceBuilderTest {
	
	@BeforeClass
	public static void init() throws Throwable {
		try {
			IntegrationTestUtil.initializeProMWithRequiredPackages("LpSolve");
		} catch (Throwable e) {
			// Ignore we only load LpSolve
		}
	}
	
	
	@Test
	public void testBuildFromFittingLog() throws ControlFlowAlignmentException, DataAlignmentException {
		
		StochasticLabelledDataPetriNet net = TestUtils.buildDataWeightTestModel();
		XLog log = TestUtils.buildTestLog();
		
		PetrinetWithMarkings markedPN = PetrinetConverter.viewAsPetrinet(net);
					
		
		Marking[] finalMarkings = List.of(PetrinetUtils.guessFinalMarking(markedPN.getNet())).toArray(new Marking[1]);
		BalancedProcessorConfiguration config = BalancedProcessorConfiguration.newDefaultInstance(
				markedPN.getNet(), markedPN.getInitialMarking(), finalMarkings, 
				log, XUtils.getDefaultClassifier(log),
				1, 1, 1, 1);
		
		XLog alignedLog = new BalancedDataXAlignmentPlugin().alignLog( markedPN.getNet(), log, config);		
		Iterable<XAlignment> alignIter = XAlignmentExtension.instance().extendLog(alignedLog);
		
		ObservationInstanceBuilder builder = new ObservationInstanceBuilder(net, 
																			alignIter, 
																			Map.of(),
																			Map.of(),
																			Map.of());
		
		SetMultimap<Integer, String> variablesWritten = SetMultimapBuilder.hashKeys().hashSetValues().build();
		
		for (int i = 0; i < net.getNumberOfTransitions(); i++) {
			int[] variablesWrittenIdx = net.getWriteVariables(i);			
			variablesWritten.putAll(i, Arrays.stream(variablesWrittenIdx).mapToObj((int varIdx) -> net.getVariableLabel(varIdx)).toList());
		}
		
		Set<String> attributesConsidered = Set.copyOf(variablesWritten.values());
		ProjectedLog projectedLog = builder.buildProjectedLog(variablesWritten, attributesConsidered);
		
		assertNotNull(projectedLog);
		assertEquals(Iterables.size(projectedLog), log.size());
		
		
	}
	
	

}
