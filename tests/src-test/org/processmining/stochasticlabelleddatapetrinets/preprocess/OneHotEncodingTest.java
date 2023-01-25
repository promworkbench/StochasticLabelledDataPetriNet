package org.processmining.stochasticlabelleddatapetrinets.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Test;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.stochasticlabelleddatapetrinet.io.OneHotEncodingSerializer;
import org.processmining.stochasticlabelleddatapetrinet.io.OneHotEncodingSerializerImpl;
import org.processmining.stochasticlabelleddatapetrinet.preprocess.OneHotEncoding;
import org.processmining.stochasticlabelleddatapetrinets.SepsisTestLog;

public class OneHotEncodingTest {
	
	@Test
	public void serializeOneHotEncodingTest() throws UnknownTreeNodeException, ReductionFailedException, Exception {

		XLog log = SepsisTestLog.loadSepsisLog();
		
		OneHotEncoding oneHotEncoding = new OneHotEncoding();
		
		oneHotEncoding.fit(log);
		
		XLog logBeforeSerialize = oneHotEncoding.process(log);

		
		Path tempFile = Files.createTempFile("onehotencoding", ".sldpn");
		
		try {
			
			OneHotEncodingSerializer serializerImpl = new OneHotEncodingSerializerImpl();
		
			try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
				serializerImpl.serialize(oneHotEncoding, fos);
			}

			try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
				OneHotEncoding oneHotEncodingDeserialized = serializerImpl.deserialize(fis);
				
				assertEquals(oneHotEncoding.getNumEncodedAttributes(), oneHotEncodingDeserialized.getNumEncodedAttributes());
				XLog logAfterSerialize = oneHotEncodingDeserialized.process(log);
				
				Iterator<XTrace> iterator1 = logBeforeSerialize.iterator();
				for (Iterator<XTrace> iterator2 = logAfterSerialize.iterator(); iterator2.hasNext();) {
					assertTrue(iterator1.hasNext());
					XTrace t1 = iterator1.next();
					XTrace t2 = iterator2.next();
					
					Iterator<XEvent> eIter1 = t1.iterator();
					for (Iterator<XEvent> eIter2 = t2.iterator(); eIter2.hasNext();) {
						assertTrue(eIter1.hasNext());
						XEvent e1 = eIter1.next();
						XEvent e2 = eIter2.next();
						assertEquals(e1.getAttributes(), e2.getAttributes());
					}
				}
			}

		} finally {
			tempFile.toFile().delete();	
		}		
		
		
	}
	

}
