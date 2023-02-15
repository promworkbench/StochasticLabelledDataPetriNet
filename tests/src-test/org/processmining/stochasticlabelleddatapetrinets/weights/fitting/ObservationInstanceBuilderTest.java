package org.processmining.stochasticlabelleddatapetrinets.weights.fitting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.processmining.stochasticlabelleddatapetrinets.IntegrationTestUtil;
import org.processmining.stochasticlabelleddatapetrinets.SimpleTestLog;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

import weka.core.Instances;

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
		
		StochasticLabelledDataPetriNet net = SimpleTestLog.buildDataWeightTestModel();
		XLog log = SimpleTestLog.buildTestLog();
		
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
					Arrays.stream(variablesWrittenIdx).mapToObj((int varIdx) -> net.getVariableLabel(varIdx)).collect(Collectors.toList()));
		}
		Set<String> attributesConsidered = Set.copyOf(variablesWritten.values());
			
		Map<String, Integer> eventClass2TransitionIdx = new HashMap<>();
		for (int i = 0; i < net.getNumberOfTransitions(); i++) {
			eventClass2TransitionIdx.put(markedPN.getTransitionIndexToId().get(i), i);
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
				attributesConsidered, eventClass2TransitionIdx);
		
		assertNotNull(projectedLog);
		assertEquals(log.size(), Iterables.size(projectedLog));
		
		ProjectedEvent event = projectedLog.iterator().next().iterator().next();
		assertEquals(0, (int) event.getActivity()); // transition A
		assertEquals(10.0, event.getAttributeValue("X"));
		
		
		Map<Integer, Multiset<Map<Integer, Object>>> instances = builder.buildInstancesMultimap(projectedLog, eventClass2TransitionIdx);
		
		System.out.println(instances);
		
		assertEquals(10, instances.get(2).size()); // B was seen 10 times
		assertEquals(10, instances.get(2).count(Map.of(0, 10.0))); // B was seen 10 times with X = 10
		assertEquals(20, instances.get(-2).count(Map.of(0, 5.0))); // non-B was seen 20 times with X = 5
		assertEquals(0, instances.get(-2).count(Map.of(0, 10.0))); // non-B never saw X = 10
		
		Instances wekaInstances = builder.buildInstances(1, instances);
		
		System.out.println(wekaInstances.toString());
		
		assertEquals(2, wekaInstances.size());
		assertEquals(2, wekaInstances.numAttributes());
		assertEquals(2, wekaInstances.numInstances());
		assertEquals(30.0, wekaInstances.sumOfWeights(), 0.01);
		
	}

	private Class<?> typeToClass(VariableType variableType) {
		return Double.class;
	}
	
	

}
