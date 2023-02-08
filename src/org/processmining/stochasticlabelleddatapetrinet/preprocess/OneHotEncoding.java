package org.processmining.stochasticlabelleddatapetrinet.preprocess;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
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

	private Set<String> excludedAttributes;
	private Map<String, Map<Object, String>> encoding = new HashMap<>();
	private int maxCategories;

	public OneHotEncoding() {
		this(new HashSet<>(), Integer.MAX_VALUE);
	}

	public OneHotEncoding(int maxCategories) {
		this(new HashSet<>(), maxCategories);
	}

	public OneHotEncoding(int maxCategories, String... excludedAttributes) {
		this(new HashSet<String>(List.of(excludedAttributes)), maxCategories);
	}

	public OneHotEncoding(Set<String> excludedAttributes, int maxCategories) {
		super();
		this.excludedAttributes = excludedAttributes;
		this.excludedAttributes.addAll(STANDARD_EXCLUDED_ATTRIBUTES);
		this.maxCategories = maxCategories;
	}

	public int getNumEncodedAttributes() {
		return encoding.size();
	}

	public void fit(XLog log) {
		Map<String, Class<?>> categoricalEventAttrs = Maps.filterValues(XUtils.getEventAttributeTypes(log),
				clazz -> clazz.isAssignableFrom(String.class));
		List<String> eventAttrLabels = categoricalEventAttrs.keySet().stream()
				.filter(s -> !excludedAttributes.contains(s)).collect(Collectors.toList());

		Map<String, Class<?>> categoricalTraceAttrs = Maps.filterValues(XUtils.getTraceAttributeTypes(log),
				clazz -> clazz.isAssignableFrom(String.class));
		List<String> traceAttrLabels = categoricalTraceAttrs.keySet().stream()
				.filter(s -> !excludedAttributes.contains(s)).collect(Collectors.toList());

		Multimap<String, Object> valueMap = HashMultimap.create();

		for (XTrace t : log) {
			recordAttributeValues(t, traceAttrLabels, valueMap);
			for (XEvent e : t) {
				recordAttributeValues(e, eventAttrLabels, valueMap);
			}
		}

		// Encode
		//TODO use statistic tests? (
		// new ChiSquareTest().chiSquareTest(counts, alpha / numberOfTests);
		for (Entry<String, Collection<Object>> entry : valueMap.asMap().entrySet()) {
			if (entry.getValue().size() <= maxCategories) {
				Map<Object, String> hotMap = new HashMap<>();
				int hotIdx = 0;
				for (Object val : entry.getValue()) {
					hotMap.put(val, entry.getKey() + "_" + hotIdx++);
				}
				hotMap.put(null, entry.getKey() + "_unknown"); // for categories it was not trained on	
				encoding.put(entry.getKey(), hotMap);
			}
		}
	}

	public XLog process(XLog log) {
		XLog pLog = (XLog) log.clone();

		// Replace with OneHot 
		for (XTrace t : pLog) {
			oneHotEncode(t, encoding);
			for (XEvent e : t) {
				oneHotEncode(e, encoding);
			}
		}

		return pLog;
	}

	public void serialize(OutputStream os) throws IOException {
		ObjectOutputStream objOut = new ObjectOutputStream(os);
		objOut.writeObject(excludedAttributes);
		objOut.writeObject(encoding);
	}

	@SuppressWarnings("unchecked")
	public void deserialize(InputStream is) throws ClassNotFoundException, IOException {
		ObjectInputStream objIn = new ObjectInputStream(is);
		excludedAttributes = (Set<String>) objIn.readObject();
		encoding = (Map<String, Map<Object, String>>) objIn.readObject();
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
		// categorical
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
		// boolean
		Collection<XAttribute> numericAttributesForBoolean = new ArrayList<XAttribute>();
		for (Iterator<XAttribute> iter = a.getAttributes().values().iterator(); iter.hasNext();) {
			XAttribute attr = iter.next();
			if (attr instanceof XAttributeBoolean) {
				iter.remove();
				numericAttributesForBoolean.add(XUtils.createAttribute(attr.getKey(),
						((Boolean) XUtils.getAttributeValue(attr)).booleanValue() ? 1 : 0));
			}
		}
		XUtils.putAttributes(a, numericAttributesForBoolean);

	}

}
