package org.processmining.stochasticlabelleddatapetrinet.weights.writeops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XUtils;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependent;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.SLDPNReplayUtils;
import org.python.google.common.collect.Sets;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class AllWriteOperationMiner implements WriteOperationMiner {

	private static final Set<String> STANDARD_EXCLUDED_ATTRIBUTES = Set.of(XConceptExtension.KEY_NAME, XConceptExtension.KEY_INSTANCE, XTimeExtension.KEY_TIMESTAMP, XLifecycleExtension.KEY_MODEL, XLifecycleExtension.KEY_TRANSITION );
	
	private final XLog log;

	private final Set<String> excludedAttributes;

	private final XEventClassifier classifier;
	
	public AllWriteOperationMiner(XLog log) {
		this(log, new XEventNameClassifier(), STANDARD_EXCLUDED_ATTRIBUTES);
	}

	public AllWriteOperationMiner(XLog log, XEventClassifier classifier, Set<String> excludedAttributes) {
		super();
		this.log = log;
		this.excludedAttributes = excludedAttributes;
		this.classifier = classifier;
	}

	public StochasticLabelledDataPetriNetWeightsDataDependent extendWithWrites(StochasticLabelledDataPetriNet<?> net) {
		
		Map<String, Class<?>> vars = XUtils.getEventAttributeTypes(log);
		Map<String, Class<?>> numericVars = Maps.filterValues(vars, clazz -> clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(Long.class));
		
		List<String> variableLabels = numericVars.keySet().stream()
				.filter(s -> !excludedAttributes.contains(s))
				.toList();
		Map<String, Integer> variableIndicies = toIndexMap(variableLabels);
		
		//same order as above
		List<VariableType> variableTypes = variableLabels.stream()
				.map(varLabel -> VariableType.fromClass(vars.get(varLabel)))
				.toList();
		
		Map<String, Integer> transitionMap = SLDPNReplayUtils.buildTransitionMap(net);
		Multimap<Integer, Integer> writes = HashMultimap.create(); //TODO use multiset of counting
		
		for (XTrace t: log) {
			for (XEvent e: t) {
				Integer tIdx = transitionMap.get(classifier.getClassIdentity(e));
				
				Set<String> attributeLabels = e.getAttributes().keySet();
				Sets.SetView<String> matchingAttrs = Sets.intersection(variableIndicies.keySet(), attributeLabels);
				
				for (String attr: matchingAttrs) {
					writes.put(tIdx, variableIndicies.get(attr)); //TODO prepare for frequency counting
				}
			}
		}
		
		List<int[]> varReads = new ArrayList<>();
		List<int[]> varWrites = new ArrayList<>();				

		for (int i = 0; i < net.getNumberOfTransitions(); i++) {
			// we read everything
			varReads.add(IntStream.range(0, variableLabels.size()).toArray());
			
			Collection<Integer> observedWrites = writes.get(i);
			
			varWrites.add(observedWrites.stream().mapToInt(idx->idx).sorted().toArray()); //TODO not sure if it should/needs to be be sorted
		}
		
		return new StochasticLabelledDataPetriNetWeightsDataDependent(net, 
				variableLabels, variableTypes, 
				varReads, varWrites);
	}

	private Map<String, Integer> toIndexMap(List<String> variableLabels) {
		Map<String, Integer> indexMap = new HashMap<>();
		for (int i = 0; i < variableLabels.size(); i++) {
			String var = variableLabels.get(i);
			if (indexMap.containsKey(var)) {
				throw new RuntimeException("Input list should not have duplicates, found twice: " + var);
			}
			indexMap.put(var, i);
		}
		return indexMap;
	}

}
 