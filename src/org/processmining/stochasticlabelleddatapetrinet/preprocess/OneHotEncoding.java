package org.processmining.stochasticlabelleddatapetrinet.preprocess;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class OneHotEncoding {

	private static final Set<String> STANDARD_EXCLUDED_ATTRIBUTES = Set.of(XConceptExtension.KEY_NAME,
			XConceptExtension.KEY_INSTANCE, XTimeExtension.KEY_TIMESTAMP, XLifecycleExtension.KEY_MODEL,
			XLifecycleExtension.KEY_TRANSITION);

	private final Set<String> excludedAttributes;

	public OneHotEncoding() {
		this(new HashSet<>());
	}

	public OneHotEncoding(Set<String> excludedAttributes) {
		super();
		this.excludedAttributes = excludedAttributes;
		this.excludedAttributes.addAll(STANDARD_EXCLUDED_ATTRIBUTES);
	}

	public XLog process(XLog log) {

		XLog pLog = (XLog) log.clone();

		Map<String, Class<?>> categoricalEventAttrs = Maps.filterValues(XUtils.getEventAttributeTypes(log),
				clazz -> clazz.isAssignableFrom(String.class) || clazz.isAssignableFrom(Boolean.class));
		List<String> eventAttrLabels = categoricalEventAttrs.keySet().stream()
				.filter(s -> !excludedAttributes.contains(s)).toList();

		Map<String, Class<?>> categoricalTraceAttrs = Maps.filterValues(XUtils.getTraceAttributeTypes(log),
				clazz -> clazz.isAssignableFrom(String.class) || clazz.isAssignableFrom(Boolean.class));
		List<String> traceAttrLabels = categoricalTraceAttrs.keySet().stream()
				.filter(s -> !excludedAttributes.contains(s)).toList();

		Multimap<String, Object> valueMap = HashMultimap.create();

		for (XTrace t : log) {
			recordAttributeValues(t, traceAttrLabels, valueMap);
			for (XEvent e : t) {
				recordAttributeValues(e, eventAttrLabels, valueMap);
			}
		}

		Map<String, Map<Object, String>> encoding = new HashMap<>();

		// Encode
		for (Entry<String, Collection<Object>> entry : valueMap.asMap().entrySet()) {
			Map<Object, String> hotMap = new HashMap<>();
			int hotIdx = 0;
			for (Object val : entry.getValue()) {
				hotMap.put(val, entry.getKey() + "_" + hotIdx++);
			}
			encoding.put(entry.getKey(), hotMap);
		}

		// Replace
		// Remove
		for (XTrace t : pLog) {
			oneHotEncode(t, encoding);
			for (XEvent e : t) {
				oneHotEncode(e, encoding);
			}
		}

		return pLog;
	}

	private void recordAttributeValues(XAttributable a, List<String> attrLabels, Multimap<String, Object> valueMap) {
		for (String attr : attrLabels) {
			XAttribute attribute = a.getAttributes().get(attr);
			if (attribute != null) {
				valueMap.put(attr, XUtils.getAttributeValue(attribute));
			}
		}
	}

	private void oneHotEncode(XAttributable a, Map<String, Map<Object, String>> oneHotEncoding) {
		for (Entry<String, Map<Object, String>> entry : oneHotEncoding.entrySet()) {
			XAttribute oldVal = a.getAttributes().remove(entry.getKey());
			if (oldVal != null) {
				Map<Object, String> hotMap = entry.getValue();
				String hotAttr = hotMap.get(XUtils.getAttributeValue(oldVal));
				for (String newAttr : hotMap.values()) {
					if (newAttr.equals(hotAttr)) {
						XUtils.putAttribute(a, XUtils.createAttribute(newAttr, 1l));
					} else {
						XUtils.putAttribute(a, XUtils.createAttribute(newAttr, 0l));
					}
				}
			}
		}
	}

}
