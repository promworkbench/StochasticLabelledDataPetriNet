package org.processmining.stochasticlabelleddatapetrinets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.junit.BeforeClass;
import org.junit.Test;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.balancedconformance.BalancedDataXAlignmentPlugin;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.controlflow.ControlFlowAlignmentException;
import org.processmining.plugins.balancedconformance.dataflow.exception.DataAlignmentException;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter.PetrinetMarkedWithMappings;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetUtils;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.ObservationInstanceBuilder;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.ProjectedEvent;
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
		
		PetrinetMarkedWithMappings markedPN = PetrinetConverter.viewAsPetrinet(net);					
		
		Marking[] finalMarkings = List.of(PetrinetUtils.guessFinalMarking(markedPN.getNet())).toArray(new Marking[1]);
		BalancedProcessorConfiguration config = BalancedProcessorConfiguration.newDefaultInstance(
				markedPN.getNet(), markedPN.getInitialMarking(), finalMarkings, 
				log, new XEventNameClassifier(),
				1, 1, 1, 1);
		
		XLog alignedLog = new BalancedDataXAlignmentPlugin().alignLog( markedPN.getNet(), log, config);		
		Iterable<XAlignment> alignIter = XAlignmentExtension.instance().extendLog(alignedLog);
		

		SetMultimap<Integer, String> variablesWritten = SetMultimapBuilder.hashKeys().hashSetValues().build();

		for (int i = 0; i < net.getNumberOfTransitions(); i++) {
			int[] variablesWrittenIdx = net.getWriteVariables(i);			
			variablesWritten.putAll(i, 
					Arrays.stream(variablesWrittenIdx).mapToObj((int varIdx) -> net.getVariableLabel(varIdx)).toList());
		}
		Set<String> attributesConsidered = Set.copyOf(variablesWritten.values());
			
		Map<String, Integer> transitionsLocalId = new HashMap<>();
		for (int i = 0; i < net.getNumberOfTransitions(); i++) {
			transitionsLocalId.put(markedPN.getTransitionIndexToId().get(i), i);
		}
		
		
		Map<String, Object> initialValues = Map.of();
		Map<String, Class<?>> variableClasses = new HashMap<>();
		Map<String, VariableType> variableTypes = new HashMap<>();

		for (int i = 0; i < net.getNumberOfVariables(); i++) {			
			VariableType variableType = net.getVariableType(i);
			variableTypes.put(net.getVariableLabel(i), variableType); 
			variableClasses.put(net.getVariableLabel(i), typeToClass(variableType));
		}
		
		
		ObservationInstanceBuilder builder = new ObservationInstanceBuilder(net, 
																			alignIter, 
																			initialValues,
																			variableClasses,
																			variableTypes);
		
		ProjectedLog projectedLog = builder.buildProjectedLog(variablesWritten, 
				attributesConsidered, transitionsLocalId);
		
		assertNotNull(projectedLog);
		assertEquals(log.size(), Iterables.size(projectedLog));
		
		ProjectedEvent event = projectedLog.iterator().next().iterator().next();
		assertEquals(0, event.getActivity()); // transition A
		assertEquals(10.0, event.getAttributeValue("X"));
		
		
		builder.buildInstancesMultimap(projectedLog, transitionsLocalId);
		
	}

	private Class<?> typeToClass(VariableType variableType) {
		return Double.class;
	}
	
	

}
