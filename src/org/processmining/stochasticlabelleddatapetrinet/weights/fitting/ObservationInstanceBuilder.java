package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.processmining.log.utils.XUtils;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetrinetSemanticsDataUnaware;
import org.processmining.xesalignmentextension.XAlignmentExtension.MoveType;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignment;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignmentMove;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ObservationInstanceBuilder {
	
	private final class ProjectedLogForDiscovery implements ProjectedLog {

		private Iterable<ProjectedTrace> projectedTraces;
		private Map<String, Object> initialValues;

		public ProjectedLogForDiscovery(Iterable<ProjectedTrace> projectedTraces, Map<String, Object> initialValues) {
			this.projectedTraces = projectedTraces;
			this.initialValues = initialValues;
		}

		public Iterator<ProjectedTrace> iterator() {
			return projectedTraces.iterator();
		}

		public Set<String> getAttributes() {
			return initialValues.keySet();
		}

		public Object getInitialValue(String attributeName) {
			return initialValues.get(attributeName);
		}

	}
	
	private static final Object NULL = new Object();

	private final class ProjectedTraceForDiscovery implements ProjectedTrace {

		private final List<ProjectedEvent> eventsForDiscovery;
		private final ImmutableMap<String, Object> attributes;
		
		private ProjectedTraceForDiscovery(XAlignment alignment, Set<String> consideredAttributes, SetMultimap<Integer, String> writtenAttributes,
				Map<String, Integer> transitionsLocalId) {

			Builder<String, Object> attributeBuilder = ImmutableMap.builder();
			XAttributeMap traceAttributes = alignment.getTrace().getAttributes();
			for (String key : consideredAttributes) {
				XAttribute attribute = traceAttributes.get(key);
				if (attribute != null) {
					attributeBuilder.put(key, XUtils.getAttributeValue(attribute));
				} else {
					if (isTreatMissingValuesAsNA()) {
						// Add will result in discovery obtaining a NULL, which is used as special value for missing!
						attributeBuilder.put(key, NULL);
					}
				}
			}
			this.attributes = attributeBuilder.build();

			this.eventsForDiscovery = new ArrayList<ProjectedEvent>(alignment.size());
			for (XAlignmentMove move : alignment) {
				MoveType type = move.getType();
				if (type != MoveType.LOG) { //TODO what about model moves?
					Integer transition = transitionsLocalId.get(move.getActivityId());
					ProjectedEventForDiscovery eventForDiscovery = new ProjectedEventForDiscovery(move,
							writtenAttributes.get(transition), transition);
					eventsForDiscovery.add(eventForDiscovery);
				}
			}
		}

		public Iterator<ProjectedEvent> iterator() {
			return eventsForDiscovery.iterator();
		}

		public Set<String> getAttributes() {
			return attributes.keySet();
		}

		public Object getAttributeValue(String varName) {
			return attributes.get(varName);
		}

	}

	private  final class ProjectedEventForDiscovery implements ProjectedEvent {

		private final Integer transition;
		private final ImmutableMap<String, Object> attributes;

		private ProjectedEventForDiscovery(XAlignmentMove move, Set<String> writtenAttributes, Integer transition) {
			this.transition = transition;
			Builder<String, Object> attributeBuilder = ImmutableMap.builder();
			XAttributeMap eventAttributes = move.getEvent().getAttributes();
			for (String key : writtenAttributes) {
				XAttribute attribute = eventAttributes.get(key);
				if (attribute != null) {
					attributeBuilder.put(key, XUtils.getAttributeValue(attribute));
				} else {
					if (isTreatMissingValuesAsNA()) {
						// Add will result in discovery obtaining a NULL, which is used as special value for missing!
						attributeBuilder.put(key, NULL);
					}
				}
			}
			this.attributes = attributeBuilder.build();
		}

		public Integer getActivity() {
			return transition;
		}

		public Set<String> getAttributes() {
			return attributes.keySet();
		}

		public Object getAttributeValue(String varName) {
			return attributes.get(varName);
		}
	}
	
	private final StochasticLabelledDataPetriNet net;
	private final Iterable<XAlignment> alignedLog;

	private final Map<String, StochasticLabelledDataPetriNet.VariableType> variableTypeMap;
	private final Map<String, Integer> variableIndexMap = new HashMap<>();
	
	private final Map<Integer, Integer> attributeIndexMap = new HashMap<>();

	private boolean isTreatMissingValuesAsNA = true;
	private boolean isUseWeights = true;
	
	private final Map<String, Object> initialValues;

	public ObservationInstanceBuilder(StochasticLabelledDataPetriNet net, 
			Iterable<XAlignment> alignedLog,
			Map<String, Object> initialValues, 
			Map<String, Class<?>> attributesForDiscovery,
			Map<String, StochasticLabelledDataPetriNet.VariableType> attributeTypes) {
		this.net = net;
		this.alignedLog = alignedLog;
		this.initialValues = initialValues;
		this.variableTypeMap = attributeTypes;
		for (int i = 0; i < net.getNumberOfVariables(); i++) {
			variableIndexMap.put(net.getVariableLabel(i), i);
		}
	}	
	
	public ProjectedLog buildProjectedLog(SetMultimap<Integer, String> attributesWritten, 
			Set<String> consideredAttributes, Map<String, Integer> transitionsLocalId) {

		Map<String, Object> initialValuesForConsideredAttributes = filterInitialAttributeByConsidered(consideredAttributes);
					
		ProjectedLog projectedLog = new ProjectedLogForDiscovery(
				Iterables.transform(alignedLog, new Function<XAlignment, ProjectedTrace>() {

					public ProjectedTrace apply(XAlignment alignment) {						
						return new ProjectedTraceForDiscovery(alignment, consideredAttributes, attributesWritten, transitionsLocalId);
					}
				}), initialValuesForConsideredAttributes);

		return projectedLog;
	}
	
	public Map<Integer, Multiset<Map<Integer, Object>>> buildInstancesMultimap(ProjectedLog projectedLog, 
			Map<String, Integer> eventClass2TransIdx) {
		
		final Map<Integer, Multiset<Map<Integer, Object>>> instances = new HashMap<>();
		for (Integer clazz : eventClass2TransIdx.values()) {
			assert clazz >= 0: "we assume non negative transitions indicies";
			// positive observations
			instances.put((clazz+1), HashMultiset.<Map<Integer, Object>>create());
			// negative observations
			instances.put(-(clazz+1), HashMultiset.<Map<Integer, Object>>create());
		}
		
		StochasticLabelledDataPetriNetSemantics semantics = new StochasticLabelledDataPetrinetSemanticsDataUnaware(net);		
		
		final Map<Integer, Object> escapedInitialAttributes = getEscapedInitialAttributes(projectedLog);
		final Map<Integer, Object> currentAttributeValues = new HashMap<>();
		
		for (ProjectedTrace trace : projectedLog) {

			// We assume the SLDPN having no data weights here, so use empty state
			semantics.setInitialState(semantics.newDataState());
			
			// Initial values for variables
			currentAttributeValues.putAll(escapedInitialAttributes);
						
			// Add values from trace
			extractAttributeValues(currentAttributeValues, trace);
			
			for (Iterator<ProjectedEvent> iterator = trace.iterator(); iterator.hasNext();) {
				
				ProjectedEvent event = iterator.next();
				
				// Add instances for enabled and chosen transitions
				
				BitSet enabled = semantics.getEnabledTransitions();
				for (int tIdx = enabled.nextSetBit(0); tIdx >= 0; tIdx = enabled.nextSetBit(tIdx + 1)) {
					// operate on index i here
					if (tIdx != event.getActivity()) {
						// negative example transition with index tIdx was not chosen
						instances.get(-(tIdx+1)).add(ImmutableMap.copyOf(currentAttributeValues));	
					}
					
					if (tIdx == Integer.MAX_VALUE) {
						break; // or (i+1) would overflow
					}
				}
				
				// positive example we chose transition corresponding to the event
				instances.get(event.getActivity() + 1).add(ImmutableMap.copyOf(currentAttributeValues));
				
				
				// execute transitions
				// we do not need to update datastate since we assume there are no relevant weights 
				semantics.executeTransition(event.getActivity(), semantics.getDataState()); 
				
				// update data values written
				extractAttributeValues(currentAttributeValues, event);
			}
			
			currentAttributeValues.clear();
		}
		
		return instances;
	}

	private void extractAttributeValues(final Map<Integer, Object> currentAttributeValues, ProjectedAttributable attr) {
		// Update current values with observations
		for (String attributeKey : attr.getAttributes()) {
			// NULL is used as marker for missing values
			Object value = attr.getAttributeValue(attributeKey);
			currentAttributeValues.put(escapeAttributeName(attributeKey), value);
		}
	}
	
	public Instances buildInstances(Integer transition, Map<Integer, Multiset<Map<Integer, Object>>> instancesMultimap) {
		
		String name = UUID.randomUUID().toString();  // dont care about the name
		
		// convention that negative class is negation (add +1 to avoid 0 as index)
		Multiset<Map<Integer, Object>> positives = instancesMultimap.get((transition+1));
		Multiset<Map<Integer, Object>> negatives = instancesMultimap.get(-(transition+1));
				
		int capacity = positives.size() + negatives.size();
		
		Instances instances = new Instances(name, createAttributes(transition), capacity);	
		instances.setClassIndex(0); // class is always first attribute
		
		addInstanceForClass(instances, 1, positives); // transition fires
		addInstanceForClass(instances, 0, negatives); // transition does not fire
		
		return instances;			
	}

	private void addInstanceForClass(Instances instances, Integer targetClass, Multiset<Map<Integer, Object>> positives) {
		for (Multiset.Entry<Map<Integer, Object>> entry : positives.entrySet()) {
			Map<Integer, Object> attributesWithNull = Maps.transformValues(entry.getElement(),
					(Function<Object, Object>) val -> {
						if (val == NULL) {
							return null;
						} else {
							return val;
						}
					});
			
			if (isUseWeights()) {
				instances.add(createInstance(instances, attributesWithNull, targetClass, entry.getCount()));
			} else {
				for (int i = 0; i < entry.getCount(); i++) {
					instances.add(createInstance(instances, attributesWithNull, targetClass, 1.0f));
				}
			}				
			
		}
	}

	private ArrayList<Attribute> createAttributes(Integer transition) {
		// plus class attribute
		ArrayList<Attribute> attributeList = new ArrayList<>(variableTypeMap.size() + 1);

		// positive and negative class
		attributeList.add(new Attribute("class", List.of("1", "0")));
				
		for (Entry<String, VariableType> entry : variableTypeMap.entrySet()) {
			Attribute attr = createAttribute(entry);
			attributeIndexMap.put(variableIndexMap.get(entry.getKey()), attributeList.size());
			attributeList.add(attr);
		} 
		return attributeList;
	}

	private Attribute createAttribute(Entry<String, VariableType> entry) {
		Attribute attr = null;
		Integer varIdx = variableIndexMap.get(entry.getKey());
		switch (entry.getValue()) {
			case DISCRETE : // Do the same as case CONTINOUS
			case CONTINUOUS :					
				attr = new Attribute(varIdx.toString());					
				break;
			case CATEGORICAL :
				// TODO 
				throw new UnsupportedOperationException("not yet supported");
		}
		return attr;
	}

	private Instance createInstance(Instances instances, Map<Integer, Object> attributesWithNull, Integer target, float weight) {
		Instance instance = new DenseInstance(attributeIndexMap.size()+1); // plus class attribute
		instance.setDataset(instances);

		instance.setWeight(weight);
		instance.setValue(instances.attribute(0), String.valueOf(target)); // Class value
		
		for (Entry<Integer, Object> entry : attributesWithNull.entrySet()) {
			
			Attribute attr = null;
			
			Integer attributeKey = entry.getKey();
			Integer attributeIndex = attributeIndexMap.get(attributeKey);
			if (attributeIndex == null) {
				throw new RuntimeException("Unknown attribute " + attributeKey);
			}
			
			attr = instances.attribute(attributeIndex);
			if (attr == null)
				continue;

			Object value = entry.getValue();
			if (value == null) {
				// NULL means there is a missing value for this attribute
				instance.setMissing(attr);
			} else {
				VariableType variableType = variableTypeMap.get(net.getVariableLabel(attributeKey));
				if (value instanceof Number && (variableType == VariableType.DISCRETE
						|| variableType == VariableType.CONTINUOUS))
					instance.setValue(attr, ((Number) value).doubleValue());
				else if (value instanceof Boolean && variableType == VariableType.CATEGORICAL) {
					if (((Boolean) value).booleanValue())
						instance.setValue(attr, 1);
					else
						instance.setValue(attr, 0);
				} else if (value instanceof String && variableType == VariableType.CATEGORICAL) {
					throw new UnsupportedOperationException("String variables not yet supported");
				} else {
					System.out.println("Skipped variable " + attributeKey + " with value " + entry.getValue());
				}
			}
		}
		return instance;
	}
	
	private boolean isUseWeights() {
		return isUseWeights;
	}

	public boolean isTreatMissingValuesAsNA() {
		return isTreatMissingValuesAsNA;
	}

	public void setTreatMissingValuesAsNA(boolean isTreatMissingValuesAsNA) {
		this.isTreatMissingValuesAsNA = isTreatMissingValuesAsNA;
	}
	
	private Map<Integer, Object> getEscapedInitialAttributes(ProjectedLog projectedLog) {
		Builder<Integer, Object> valueBuilder = ImmutableMap.builder();
		for (String attribute : projectedLog.getAttributes()) {
			Object value = projectedLog.getInitialValue(attribute);
			if (value != null) {
				valueBuilder.put(escapeAttributeName(attribute), value);
			} else {
				valueBuilder.put(escapeAttributeName(attribute), NULL);
			}
		}
		return valueBuilder.build();
	}	
	
	private Map<String, Object> filterInitialAttributeByConsidered(final Set<String> consideredAttributes) {
		Map<String, Object> initialValuesForConsideredAttributes = Maps
				.newHashMapWithExpectedSize(consideredAttributes.size());
		for (String considered : consideredAttributes) {
			initialValuesForConsideredAttributes.put(considered, initialValues.get(considered));
		}
		return initialValuesForConsideredAttributes;
	}
	
	private Integer escapeAttributeName(String attrName) {
		return variableIndexMap.get(attrName);
	}	

}